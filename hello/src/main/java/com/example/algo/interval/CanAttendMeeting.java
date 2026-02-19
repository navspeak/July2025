package com.example.algo.interval;

import java.util.Arrays;

public class CanAttendMeeting {
    public Boolean canAttendMeetings(int[][] intervals) {
        if (intervals == null || intervals.length <= 1) return true;
        Arrays.sort(intervals, (a, b) -> Integer.compare(a[0], b[0]));
        int prevEnd = intervals[0][1];
        // |-------------|
        //                |---------|
        // |----------------------------|
        //                |---------|
        for (int i = 1; i < intervals.length; i++) {
            int thisStart = intervals[i][0];
            int thisEnd = intervals[i][1];
            if (thisStart<prevEnd) return false;
            if (thisEnd<prevEnd) return false;
            prevEnd = thisEnd;

        }

        return true;
    }

    public static void main(String[] args) {
        int[][] intervals = {
                {1, 5},
                {6, 8},
                {3, 9},

        };
    }
}
/*
Write a function to check if a person can attend all the meetings scheduled without any time conflicts.
Given an array intervals, where each element [s1, e1] represents a meeting starting at time s1 and ending at time e1,
determine if there are any overlapping meetings. If there is no overlap between any meetings, return true; otherwise,
 return false.
 */