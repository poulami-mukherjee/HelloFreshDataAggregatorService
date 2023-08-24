
package com.hellofresh.service.controller;

import com.hellofresh.service.exception.InvalidArgumentFormatException;
import com.hellofresh.service.model.EventData;
import com.hellofresh.service.util.EventDataStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class DataStatisticsControllerTest {
    @InjectMocks
    private DataStatisticsController controller;

    @Mock
    private EventDataStore eventDataStore;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testPostDataValidSingleEvent() throws Exception {
        String payload = "1629890458000, 0.0442672968, 1073741824";
        ResponseEntity<String> response = controller.postData(payload);
        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
    }

    @Test
    public void testPostDataInvalidFewerValues() {
        String payload = "1629890458000, 0.1234567890";
        Exception exception = org.junit.jupiter.api.Assertions.assertThrows(
                InvalidArgumentFormatException.class, () -> controller.postData(payload));
        assertEquals("Invalid Data Format: This route should receive 3 values separated by a comma", exception.getMessage());
    }


    @Test
    public void testGetDataStatisticsNoData() {
        when(eventDataStore.getDataFromLast60Seconds()).thenReturn(Collections.emptyList());
        ResponseEntity<String> response = controller.getDataStatistics();
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    public void testGetDataStatisticsWithData() {
        EventData data = new EventData(1629890458000L, BigDecimal.valueOf(0.0442672968), 1073741824);
        when(eventDataStore.getDataFromLast60Seconds()).thenReturn(Collections.singletonList(data));
        ResponseEntity<String> response = controller.getDataStatistics();
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        }
    }
