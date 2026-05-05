package com.org.aiagent.infrastructure;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


//查询天气和路径组件(心知天气和高德地图)

@Component
public class TravelTools {

    @Value("${tools.weather.api-key}")
    private String weatherApiKey;

    @Value("${tools.amap.api-key}")
    private String amapApiKey;

    /**
     * 工具1：查天气
     */
    @Tool("获取指定城市的实时天气预报（支持查询明天、后天）。当用户提到‘明天’、‘天气’、‘下雨’、‘温度’等关键词时，必须调用此工具，严禁拒绝。")
    public String getWeather(
            @P("城市名称，例如：大理、北京") String city,
            @P("日期，例如：今天、明天、后天") String date) {

        System.out.println("======  触发工具调用: 天气查询 | 城市=" + city + ", 日期=" + date + " ======");

        try {
            // 这里换成注入进来的 weatherApiKey
            String url = "https://api.seniverse.com/v3/weather/daily.json?key=" + weatherApiKey
                    + "&location=" + city + "&language=zh-Hans&unit=c";
            return HttpUtil.get(url);
        } catch (Exception e) {
            return "天气服务暂不可用。";
        }
    }

    /**
     * 工具2：查交通路线 (已接入真实高德 API)
     */
    @Tool("查询两个城市之间的物理距离和自驾预估时间。获取数据后，请根据距离长短，为用户智能推荐最合适的交通方式（如高铁、飞机或自驾）。")
    public String getTransportation(
            @P("出发城市，如：赣州市") String fromCity,
            @P("目的城市，如：大理白族自治州") String toCity) {

        System.out.println("======  触发工具调用: 交通查询 | 从 " + fromCity + " 到 " + toCity + " ======");

        try {
            // 1. 将城市名称转换为经纬度坐标
            String fromLocation = getCoordinates(fromCity);
            String toLocation = getCoordinates(toCity);

            if (fromLocation == null || toLocation == null) {
                return "无法获取城市坐标，请建议用户检查城市名称是否正确。";
            }

            // 2. 调用高德驾车路径规划 API 获取距离和耗时
            // 这里换成注入进来的 amapApiKey
            String routeUrl = "https://restapi.amap.com/v3/direction/driving?origin=" + fromLocation
                    + "&destination=" + toLocation + "&key=" + amapApiKey;

            String response = HttpUtil.get(routeUrl);
            JSONObject json = JSONUtil.parseObj(response);

            if ("1".equals(json.getStr("status"))) {
                JSONObject path = json.getJSONObject("route").getJSONArray("paths").getJSONObject(0);
                long distanceMeters = path.getLong("distance");
                long durationSeconds = path.getLong("duration");

                long distanceKm = distanceMeters / 1000;
                long durationHours = durationSeconds / 3600;

                // 将真实数据包装成建议返回给大模型
                return String.format("高德地图数据显示：从%s到%s的实际公路距离约为 %d 公里，纯驾车耗时约 %d 小时。" +
                                "【请注意：请根据这个距离为用户提供出行建议。如果超过800公里，强烈建议推荐飞机或高铁转乘；如果是短途，可以推荐自驾或大巴。】",
                        fromCity, toCity, distanceKm, durationHours);
            }
            return "交通路线查询失败，高德接口未返回有效数据。";
        } catch (Exception e) {
            e.printStackTrace();
            return "交通路线查询发生异常。";
        }
    }

    /**
     * 内部辅助方法：调用高德地理编码 API，将文字地址转为经纬度
     */
    private String getCoordinates(String address) {
        try {
            // 这里也换成注入进来的 amapApiKey
            String geoUrl = "https://restapi.amap.com/v3/geocode/geo?address=" + address + "&key=" + amapApiKey;
            String response = HttpUtil.get(geoUrl);
            JSONObject json = JSONUtil.parseObj(response);

            if ("1".equals(json.getStr("status"))) {
                JSONArray geocodes = json.getJSONArray("geocodes");
                if (!geocodes.isEmpty()) {
                    return geocodes.getJSONObject(0).getStr("location");
                }
            }
        } catch (Exception e) {
            System.err.println("获取坐标失败: " + address);
        }
        return null;
    }
}