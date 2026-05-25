package com.shengyi.fec.reimbursement.service;

import com.shengyi.fec.reimbursement.entity.CityTierEntity;
import com.shengyi.fec.reimbursement.mapper.CityTierMapper;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class CityTierCache {
    private final CityTierMapper cityTierMapper;
    private final Map<String, String> cityNoToTier = new ConcurrentHashMap<>();
    private final Map<String, String> cityNameToTier = new ConcurrentHashMap<>();

    @PostConstruct
    public void load() {
        List<CityTierEntity> list = cityTierMapper.selectList(null);
        for (CityTierEntity c : list) {
            cityNoToTier.put(c.getCityNo(), c.getTier());
            cityNameToTier.put(c.getCityName(), c.getTier());
        }
    }

    public BigDecimal mealStandard(String cityNo, String cityName) {
        String tier = cityNo != null ? cityNoToTier.get(cityNo) : null;
        if (tier == null && cityName != null) {
            tier = cityNameToTier.get(cityName);
        }
        if (tier == null) {
            tier = "3";
        }
        if ("1".equals(tier)) {
            return BigDecimal.valueOf(100);
        }
        if ("2".equals(tier)) {
            return BigDecimal.valueOf(80);
        }
        return BigDecimal.valueOf(50);
    }

    public Map<String, Object> allCities() {
        Map<String, Object> result = new HashMap<>();
        result.put("cities", cityTierMapper.selectList(null));
        return result;
    }
}
