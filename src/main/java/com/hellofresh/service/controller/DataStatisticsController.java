package com.hellofresh.service.controller;

import com.hellofresh.service.exception.InvalidArgumentFormatException;
import com.hellofresh.service.exception.InvalidXValueException;
import com.hellofresh.service.exception.InvalidYValueException;
import com.hellofresh.service.model.EventData;
import com.hellofresh.service.util.EventDataStore;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@RestController
public class DataStatisticsController {
    private final EventDataStore eventDataStore = new EventDataStore();
    @PostMapping("/event")
    /**
     * Post event data
     * POST /event
     * Description:
     * This endpoint posts a String containing 3 numerical values separated by a comma.
     * timestamp: An integer with the Unix timestamp in millisecond resolution when the event happened.
     *              The data is not ordered by this timestamp, this means that you may receive old data in any row.
     * 𝑥: A real number with a fractional part of up to 10 digits, always in 0..1.
     * 𝑦: An integer in 1,073,741,823..2,147,483,647.
     * Responses:
     * - 202 OK: Successfully submitted the post request.
     * - 500 Internal Server Error: Unexpected server error.
     * - 400 Bad Request: Invalid Input format
     * Assumption: Accepts a single string separated by comma
     */
    public ResponseEntity<String> postData(@RequestBody String payload) throws Exception {
        // Splitting by newline
        String[] lines = payload.split("\n");
        for (String line : lines) {
            // Splitting each line by comma
            String[] values = line.split(",");
            if (values.length != 3) {
                throw new InvalidArgumentFormatException("Invalid Data Format: " +
                        "This route should receive 3 values separated by a comma");
            }
            try {
                long timestamp = Long.parseLong(values[0].trim());
                BigDecimal x = BigDecimal.valueOf(Double.parseDouble(values[1].trim()));
                int y = Integer.parseInt(values[2].trim());
                EventData eventData = new EventData(timestamp, x, y);
                eventDataStore.store(eventData);
            }
            catch(InvalidArgumentFormatException exception) {
                throw new InvalidArgumentFormatException(exception.getMessage());
            }
            catch (InvalidXValueException exception) {
                throw new InvalidXValueException(exception.getMessage());
            }
            catch (InvalidYValueException exception) {
                throw new InvalidYValueException(exception.getMessage());
            }
            catch(Exception exception) {
                throw new Exception(exception.getMessage());
            }
        }
        return ResponseEntity.accepted().body("Event posting is ongoing");
    }

    @RequestMapping("/stats")
    /**
     * Get statistics of event data
     * GET /stats
     * Description:
     * This endpoint gets the statistic of event data
     * Responses:
     * - 200 OK: Successfully retrieved the String of comma separated data statistics.
     * - 204 OK: Successfully retrieved empty string because of no data over the period of 60 seconds
     * - 500 Internal Server Error: Unexpected server error.
     */
    public ResponseEntity<String> getDataStatistics() {

        List<EventData> recentData = eventDataStore.getDataFromLast60Seconds();

        //This also handles division by zero edge cases while finding average
        if (recentData.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        BigDecimal sumX = BigDecimal.ZERO;
        long sumY = 0;

        for (EventData data : recentData) {
            sumX = sumX.add(data.x());
            sumY += data.y();
        }

        BigDecimal avgX = sumX.divide(BigDecimal.valueOf(recentData.size()), 10, RoundingMode.HALF_UP);
        long avgY = sumY / recentData.size();

        String response = String.format("%d, %.10f, %.10f, %d, %d",
                recentData.size(), sumX, avgX, sumY, avgY);

        return ResponseEntity.ok(response);
    }
}
