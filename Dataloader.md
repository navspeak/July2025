## Big Picture
- Without DataLoader (N+1)
    - author() resolver → userService.getUserById() → repo.findById()
    - Repeated for every post.
- With DataLoader (batched)
    -   author() resolver → loader.load(id) (queues ids)
    - Later in same request:
      - DataLoader calls batch function once → userService.getUsersByIds(ids) → repo.findByIdIn(ids)

### Basically...
- Replace ` return userService.getUserById(post.getAuthorId())` with `return loader.load(post.getAuthorId());`
  - where `DataLoader<String, User> loader = env.getDataLoader("userLoader");` and `DataFetchingEnvironment env`
- So What is DFE or `DataFetchingEnvironment`:
  - a GraphQL Java object that represents the current field resolution context.
  - when Graphql is resolving the field Post.author. DFE contains things like: 
    - the field name being resolved (author)
    - the parent object (the Post instance)
    - arguments passed to the field (if any)
    - the request’s GraphQLContext
    - and importantly for you: the DataLoaderRegistry for this request, so you can do env.getDataLoader("userLoader")

So: DFE is the bridge from your resolver to the request-scoped DataLoader.

### Key idea:
* Your resolver runs many times (once per Post). Each time, it calls:
```java
    DataLoader<String, User> loader = env.getDataLoader("userLoader");
    return loader.load(post.getAuthorId());
```
* That does 2 things:
  * Looks up the DataLoader instance from the request registry (via DFE)
  * Queues the key (authorId) inside that DataLoader and returns a CompletableFuture<User>
  * It does not hit the DB immediately.

### who triggers the batch call?
- Inside GraphQL Java’s execution, DataLoader is integrated by a “dispatching” step (in GraphQL Java terms: DataLoader instrumentation / dispatch strategy).
Spring for GraphQL sets this up for you.
- In one request, the flow looks like this:
  1. GraphQL resolves posts → returns List<Post>
  2. GraphQL starts resolving Post.author for each Post
  3. Each resolver call does loader.load(authorId):
     4. DataLoader collects keys: [u1, u9, u2, u1, ...]
     5. returns futures immediately 
  6. GraphQL reaches a point where it must wait for those futures → it dispatches DataLoader work 
  7. Dispatch calls your BatchLoader once with the collected keys
  8. Your batchLoader does one DB call and completes all the queued futures

That’s the batching.

### Why do we need DataFetchingEnvironment at all?
- Because the DataLoader is not a Spring singleton you should @Autowired into your controller. It is request-scoped, created per GraphQL request inside the DataLoaderRegistry.
- So you need a way to access “the current request’s registry”. That’s what DFE gives you:
- `env.getDataLoader("userLoader")` → “give me the DataLoader instance for this request”.
- If you autowired DataLoader<String, User> directly, you’d accidentally share cache across requests (bad) and likely break batching semantics.

### Quick mental model (one-liner)
- DataFetchingEnvironment = “current field execution context”
- It connects your resolver to the request’s DataLoaderRegistry
- load() queues keys
- GraphQL dispatch triggers your BatchLoader(ids) once per batch
- Your BatchLoader does one repo call and maps results back


## Graphql Schema
```
type Query {
  users: [User!]!
  posts: [Post!]!
}

type Post {
  id: ID!
  authorId: ID!
  author: User
}

type User {
  id: ID!
  name: String!
}
```
```java
@Controller
public class PostController {
    
    @Autowired private final PostService postService;
    @Autowired private final UserService userService;
    
    @QueryMapping
    public List<Post> posts() {
        return postService.getAllPosts();
    }

    // This is the N+1: called once per Post
    @SchemaMapping(typeName = "Post", field = "author")
    public User author(Post post) {
        return userService.getUserById(post.getAuthorId()).orElse(null);
    }
    //If posts() returns 100 posts, GraphQL will call author() 100 times → 100 DB calls.
}

public interface UserRepository extends JpaRepository<User, String> {
    // Optional: explicit "IN" query (often faster / clearer than findAllById)
    List<User> findByIdIn(Collection<String> ids);
}

@Service
public class UserService {
    @Autowired private final UserRepository repo;
    public List<User> getUsersByIds(Collection<String> ids) {
        return repo.findByIdIn(ids);
        // or: return repo.findAllById(ids);
    }

    public Optional<User> getUserById(String id) {
        return repo.findById(id);
    }
}

public class User {
    @Id
    private String id;
    private String name;
}

```

* DataLoader sits between your GraphQL field resolver (controller) and your service/repo.
    * Instead of calling userService.getUserById() per post, you call: `dataLoader.load(authorId)` many times
    * DataLoader batches those ids and calls the DB once via your batch function

## Create a DataLoader (batch function calls Service → Repo)
```java
// Use a dedicated executor because JPA calls are blocking.

@Configuration
public class AsyncConfig {
    @Bean
    public Executor dbExecutor() {
        return Executors.newFixedThreadPool(16);
    }
}

@Component
public class UserDataLoader {

    @Autowired private final UserService userService;
    @Autowired private final Executor dbExecutor;


    public DataLoader<String, User> create() {
        BatchLoader<String, User> batchLoader = ids ->
            CompletableFuture.supplyAsync(() -> {
                // dedupe to reduce DB work (optional but common)
                List<String> unique = ids.stream().distinct().toList();

                List<User> users = userService.getUsersByIds(unique);

                Map<String, User> map = users.stream()
                    .collect(Collectors.toMap(User::getId, u -> u));

                // return results aligned to input ids
                return ids.stream().map(map::get).toList();
            }, dbExecutor);

        return DataLoaderFactory.newDataLoader(batchLoader);
    }
}

// Wire it per request (this is “request-scoped”)
@Component
public class MyDataLoaderRegistryFactory implements org.springframework.graphql.execution.DataLoaderRegistryFactory {

    @Autowired private final UserDataLoader userDataLoader;

    @Override
    public org.dataloader.DataLoaderRegistry createDataLoaderRegistry() {
        var registry = new org.dataloader.DataLoaderRegistry();
        registry.register("userLoader", userDataLoader.create());
        return registry;
    }
}


// Why is it request-scoped?
// Because Spring for GraphQL creates a new DataLoaderRegistry for each GraphQL HTTP request, 
// so the DataLoader instance + its cache live only for that request.

// Controller updated to use DataLoader (batched ✅)
@Controller
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @QueryMapping
    public List<Post> posts() {
        return postService.getAllPosts();
    }

    // Now batched
    @SchemaMapping(typeName = "Post", field = "author")
    public CompletableFuture<User> author(Post post, DataFetchingEnvironment env) {
        DataLoader<String, User> loader = env.getDataLoader("userLoader");
        return loader.load(post.getAuthorId());
    }
    
     // This is the N+1: called once per Post
    /* @SchemaMapping(typeName = "Post", field = "author")
    public User author(Post post) {
        return userService.getUserById(post.getAuthorId()).orElse(null);
    } */
}
// Now 100 posts → 100 load() calls → 1 batch → 1 DB call (typically).
// Earlier If posts() returns 100 posts, GraphQL will call author() 100 times → 100 DB calls.