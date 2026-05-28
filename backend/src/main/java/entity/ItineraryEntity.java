package entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("fk_reim_itinerary")
public class ItineraryEntity {
    @TableId
    private String id;
    private String mainId;
    private String travelerId;
    private String travelerNo;
    private String travelerName;
    private String departureDate;
    private String arrivalDate;
    private String departureCity;
    private String departureCityNo;
    private String arrivingCity;
    private String arrivingCityNo;
    private String itineraryInstructions;
}
