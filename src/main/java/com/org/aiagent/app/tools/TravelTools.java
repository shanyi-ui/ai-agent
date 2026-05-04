package com.org.aiagent.app.tools;

import cn.hutool.http.HttpUtil;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;

@Component
public class TravelTools {

    // 确保这里填入的是心知天气的【私钥】
    private static final String WEATHER_API_KEY = "=";

    /**
     * 工具1：查天气
     * 增加 date 参数，诱导大模型进行调用
     */
    @Tool("获取指定城市的实时天气预报（支持查询明天、后天）。当用户提到‘明天’、‘天气’、‘下雨’、‘温度’等关键词时，必须调用此工具，严禁拒绝。")
    public String getWeather(
            @P("城市名称，例如：大理、北京") String city,
            @P("日期，例如：今天、明天、后天") String date) {

        System.out.println("====== 🚨 触发工具调用: 城市=" + city + ", 日期=" + date + " ======");

        try {
            // 免费版接口
            String url = "https://api.seniverse.com/v3/weather/daily.json?key=" + WEATHER_API_KEY
                    + "&location=" + city + "&language=zh-Hans&unit=c";
            return HttpUtil.get(url);
        } catch (Exception e) {
            return "天气服务暂不可用。";
        }
    }

    /**
     * 工具2：查交通路线 (保持逻辑，优化描述)
     */
    @Tool("查询两个城市之间的交通出行方案、耗时及路线。")
    public String getTransportation(
            @P("出发城市") String fromCity,
            @P("目的城市") String toCity) {
        System.out.println("====== 🚨 触发工具调用: 从 " + fromCity + " 到 " + toCity + " ======");

        // 模拟返回
        if (fromCity.contains("赣州") && toCity.contains("大理")) {
            return "建议方案：赣州西站乘高铁至昆明南站（约6.5小时），同站换乘高铁至大理站（约2小时），全程约8.5小时。";
        }
        return "建议乘坐高铁或飞机，具体班次请查阅实时票务系统。";
    }
}