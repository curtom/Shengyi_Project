package entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("fk_city_tier")
public class CityTierEntity {
    @TableId
    private String cityNo;
    private String cityName;
    private String tier;
}
