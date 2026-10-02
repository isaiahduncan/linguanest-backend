package com.linguanest.backend.chat;

/** Clean, structured error body returned by {@link ChatExceptionHandler}. */
record ErrorResponse(String message) {
}
