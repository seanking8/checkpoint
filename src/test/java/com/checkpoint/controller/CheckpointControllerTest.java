package com.checkpoint.controller;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CheckpointControllerTest {

    @Test
    void testRootForward() {
        CheckpointController checkpointController = new CheckpointController();

        String result = checkpointController.root();

        assertEquals("forward:/index.html", result);
    }
}

