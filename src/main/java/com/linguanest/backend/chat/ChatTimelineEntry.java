package com.linguanest.backend.chat;

import java.time.Instant;

/**
 * One entry in a chat's chronological timeline (the scrolling chat view).
 *
 * <p>The spec calls for exercises, submissions, and graded results to be "merged and ordered
 * chronologically." A graded result has no independent place in that conversation - it is always
 * shown together with the submission it grades - so this merge produces exactly two kinds of
 * entries, sorted by {@link #createdAt()}:
 * <ul>
 *   <li>{@link ExerciseTimelineEntry} - a worksheet (with its questions) was generated in this chat.</li>
 *   <li>{@link SubmissionTimelineEntry} - the user submitted an answer for one of the chat's
 *       exercises, with its {@link GradedResultDto} embedded directly once grading has completed
 *       (null while the submission is still {@code PENDING}/{@code PROCESSING}, or if it
 *       {@code FAILED}).</li>
 * </ul>
 */
sealed interface ChatTimelineEntry permits ExerciseTimelineEntry, SubmissionTimelineEntry {

    Instant createdAt();
}
