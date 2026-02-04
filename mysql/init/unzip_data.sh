#!/bin/bash
set -e  # Exit immediately on any error

ZIP_FILE="/data/california_collisions.zip"
DEST_DIR="/tmp/unzipped_data"
DB_NAME="california_collisions"
MYSQL_USER="root"
MYSQL_PASS="root"

echo "📦 Starting unzip process..."

if [ ! -f "$ZIP_FILE" ]; then
  echo "❌ ZIP file not found at $ZIP_FILE"
  exit 1
fi

mkdir -p "$DEST_DIR"
echo "🗂  Unzipping data to $DEST_DIR ..."
unzip -o "$ZIP_FILE" -d "$DEST_DIR"
cp -r /data/*.sql "$DEST_DIR"/california_collisions/
echo "List of data sql:"
echo "-----------------"
ls -ltr

# Check if there are any .sql files
if ls "$DEST_DIR"/california_collisions/*.sql 1> /dev/null 2>&1; then
  echo "🧩 Found SQL files — importing..."
  for sql_file in "$DEST_DIR"/california_collisions/*.sql; do
    echo "▶️ Importing $sql_file ..."
    mysql -u"$MYSQL_USER" -p"$MYSQL_PASS" "$DB_NAME" < "$sql_file"
  done
  echo "✅ SQL import completed."
else
  echo "ℹ️ No .sql files found inside the zip. Skipping import."
fi

echo "✅ Unzip + data load finished successfully!"
