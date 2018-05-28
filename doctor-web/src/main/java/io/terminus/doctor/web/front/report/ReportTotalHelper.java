package io.terminus.doctor.web.front.report;

import io.terminus.doctor.common.enums.IsOrNot;
import io.terminus.doctor.event.util.EventUtil;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author xjn
 * @date 18/5/28
 * email xiaojiannan@terminus.io
 */
public class ReportTotalHelper {

    public static List<Map<String, String>> totalBoar(List<Map<String, String>> maps, Integer isNecessaryTotal){
        if (!Objects.equals(isNecessaryTotal, IsOrNot.YES.getKey())) {
            return maps;
        }

        Map<String, String> total = maps.get(maps.size() - 1);
        for (int i = 0; i < maps.size() -1 ; i++) {
            Map<String, String> map = maps.get(i);
            if (!Objects.equals(map.get("dead"), "-")) {
                total.put("dead", add(map.get("dead"), total.get("dead")));
            }
            if (!Objects.equals(map.get("weedOut"), "-")) {
                total.put("weedOut", add(map.get("weedOut"), total.get("weedOut")));
            }
            if (!Objects.equals(map.get("sale"), "-")) {
                total.put("sale", add(map.get("sale"), total.get("sale")));
            }
        }
        return maps;
    }


    public static List<Map<String, String>> totalDeliver(List<Map<String, String>> maps, Integer isNecessaryTotal){
        if (!Objects.equals(isNecessaryTotal, IsOrNot.YES.getKey())) {
            return maps;
        }

        Map<String, String> total = maps.get(maps.size() - 1);
        for (int i = 0; i < maps.size() -1 ; i++) {
            Map<String, String> map = maps.get(i);
            if (!Objects.equals(map.get("sowPhWeanOut"), "-")) {
                total.put("sowPhWeanOut", add(map.get("sowPhWeanOut"), total.get("sowPhWeanOut")));
            }

            if (!Objects.equals(map.get("farrowNest"), "-")) {
                total.put("farrowNest", add(map.get("farrowNest"), total.get("farrowNest")));
            }

            if (!Objects.equals(map.get("toNursery"), "-")) {
                total.put("toNursery", add(map.get("toNursery"), total.get("toNursery")));
            }

            if (!Objects.equals(map.get("weanNest"), "-")) {
                total.put("weanNest", add(map.get("weanNest"), total.get("weanNest")));
            }

            if (!Objects.equals(map.get("pigletSale"), "-")) {
                total.put("pigletSale", add(map.get("pigletSale"), total.get("pigletSale")));
            }
        }

        if (!Objects.equals(total.get("farrowNest"), "-")) {
            String farrowNest = total.get("farrowNest");
            total.put("pigletCountPerFarrow", div(total.get("farrowAll"), farrowNest));
            total.put("pigletLivingCountPerFarrow", div(total.get("farrowLiving"), farrowNest));
            total.put("pigletHealthCountPerFarrow", div(total.get("farrowHealth"), farrowNest));
            total.put("pigletWeakCountPerFarrow", div(total.get("farrowWeak"), farrowNest));
            total.put("avgWeightPerFarrow", div(total.get("firstBornWeight"), farrowNest));
        }

        return maps;
    }

    public static List<Map<String, String>> totalFatten(List<Map<String, String>> maps, Integer isNecessaryTotal){
        if (!Objects.equals(isNecessaryTotal, IsOrNot.YES.getKey())) {
            return maps;
        }

        Map<String, String> total = maps.get(maps.size() - 1);
        for (int i = 0; i < maps.size() -1 ; i++) {
            Map<String, String> map = maps.get(i);
            total.put("turnInto", add(map.get("turnInto"), total.get("turnInto")));
            total.put("turnIntoAvgWeight", add(multi(map.get("turnIntoAvgWeight"), map.get("turnInto")), total.get("turnIntoAvgWeight")));
            total.put("sale", add(map.get("sale"), total.get("sale")));
            total.put("saleAvgWeight", add(multi(map.get("saleAvgWeight"), map.get("sale")), total.get("saleAvgWeight")));
            total.put("toHoubei", add(map.get("toHoubei"), total.get("toHoubei")));
            total.put("toHoubeiAvgWeight", add(multi(map.get("toHoubeiAvgWeight"), map.get("toHoubei")), total.get("toHoubeiAvgWeight")));
            total.put("chgFarmOut", add(map.get("chgFarmOut"), total.get("chgFarmOut")));
            total.put("chgFarmAvgWeight", add(multi(map.get("chgFarmAvgWeight"), map.get("chgFarmOut")), total.get("chgFarmAvgWeight")));
            total.put("dead", add(map.get("dead"), total.get("dead")));
            total.put("weedOut", add(map.get("weedOut"), total.get("weedOut")));
            total.put("otherChange", add(map.get("otherChange"), total.get("otherChange")));

        }

        total.put("turnIntoAvgWeight", div(total.get("turnIntoAvgWeight"), total.get("turnInto")));
        total.put("saleAvgWeight", div(total.get("saleAvgWeight"), total.get("sale")));
        total.put("toHoubeiAvgWeight", div(total.get("toHoubeiAvgWeight"), total.get("toHoubei")));
        total.put("chgFarmAvgWeight", div(total.get("chgFarmAvgWeight"), total.get("chgFarmOut")));

        return maps;
    }

    public static List<Map<String, String>> totalMating(List<Map<String, String>> maps, Integer isNecessaryTotal) {
        if (!Objects.equals(isNecessaryTotal, IsOrNot.YES.getKey())) {
            return maps;
        }

        Map<String, String> total = maps.get(maps.size() - 1);
        for (int i = 0; i < maps.size() - 1; i++) {
            Map<String, String> map = maps.get(i);
            total.put("sowPhWeanIn", add(map.get("sowPhWeanIn"), total.get("sowPhWeanIn")));
            total.put("pregPositive", add(map.get("pregPositive"), total.get("pregPositive")));
            total.put("pregNegative", add(map.get("pregNegative"), total.get("pregNegative")));
            total.put("pregFanqing", add(map.get("pregFanqing"), total.get("pregFanqing")));
            total.put("pregLiuchan", add(map.get("pregLiuchan"), total.get("pregLiuchan")));

        }
        return maps;
    }

    public static List<Map<String, String>> totalNursery(List<Map<String, String>> maps, Integer isNecessaryTotal) {
        if (!Objects.equals(isNecessaryTotal, IsOrNot.YES.getKey())) {
            return maps;
        }

        Map<String, String> total = maps.get(maps.size() - 1);
        for (int i = 0; i < maps.size() - 1; i++) {
            Map<String, String> map = maps.get(i);
            total.put("turnInto", add(map.get("turnInto"), total.get("turnInto")));
            total.put("turnIntoAvgWeight", add(multi(map.get("turnIntoAvgWeight"), map.get("turnInto")), total.get("turnIntoAvgWeight")));
            total.put("sale", add(map.get("sale"), total.get("sale")));
            total.put("saleAvgWeight", add(multi(map.get("saleAvgWeight"), map.get("sale")), total.get("saleAvgWeight")));
            total.put("toFatten", add(map.get("toFatten"), total.get("toFatten")));
            total.put("toFattenAvgWeight", add(multi(map.get("toFattenAvgWeight"), map.get("toFatten")), total.get("toFattenAvgWeight")));
            total.put("toHoubei", add(map.get("toHoubei"), total.get("toHoubei")));
            total.put("toHoubeiAvgWeight", add(multi(map.get("toHoubeiAvgWeight"), map.get("toHoubei")), total.get("toHoubeiAvgWeight")));
            total.put("chgFarmOut", add(map.get("chgFarmOut"), total.get("chgFarmOut")));
            total.put("chgFarmAvgWeight", add(multi(map.get("chgFarmAvgWeight"), map.get("chgFarmOut")), total.get("chgFarmAvgWeight")));
            total.put("dead", add(map.get("dead"), total.get("dead")));
            total.put("weedOut", add(map.get("weedOut"), total.get("weedOut")));
            total.put("otherChange", add(map.get("otherChange"), total.get("otherChange")));

        }

        total.put("turnIntoAvgWeight", div(total.get("turnIntoAvgWeight"), total.get("turnInto")));
        total.put("saleAvgWeight", div(total.get("saleAvgWeight"), total.get("sale")));
        total.put("toFattenAvgWeight", div(total.get("toFattenAvgWeight"), total.get("toFatten")));
        total.put("toHoubeiAvgWeight", div(total.get("toHoubeiAvgWeight"), total.get("toHoubei")));
        total.put("chgFarmAvgWeight", div(total.get("chgFarmAvgWeight"), total.get("chgFarmOut")));

        return maps;
    }

    public static List<Map<String, String>> totalReserve(List<Map<String, String>> maps, Integer isNecessaryTotal) {
        if (!Objects.equals(isNecessaryTotal, IsOrNot.YES.getKey())) {
            return maps;
        }

        Map<String, String> total = maps.get(maps.size() - 1);
        for (int i = 0; i < maps.size() - 1; i++) {
            Map<String, String> map = maps.get(i);
            total.put("turnInto", add(map.get("turnInto"), total.get("turnInto")));
            total.put("turnSeed", add(map.get("turnSeed"), total.get("turnSeed")));
            total.put("dead", add(map.get("dead"), total.get("dead")));
            total.put("weedOut", add(map.get("weedOut"), total.get("weedOut")));
            total.put("toFatten", add(map.get("toFatten"), total.get("toFatten")));
            total.put("sale", add(map.get("sale"), total.get("sale")));
            total.put("chgFarmOut", add(map.get("chgFarmOut"), total.get("chgFarmOut")));
            total.put("otherChange", add(map.get("otherChange"), total.get("otherChange")));

        }
        return maps;
    }

    public static List<Map<String, String>> totalSow(List<Map<String, String>> maps, Integer isNecessaryTotal) {
        if (!Objects.equals(isNecessaryTotal, IsOrNot.YES.getKey())) {
            return maps;
        }

        Map<String, String> total = maps.get(maps.size() - 1);
        for (int i = 0; i < maps.size() - 1; i++) {
            Map<String, String> map = maps.get(i);
            if (!Objects.equals(map.get("dead"), "-")) {
                total.put("dead", add(map.get("dead"), total.get("dead")));
            }
            if (!Objects.equals(map.get("weedOut"), "-")) {
                total.put("weedOut", add(map.get("weedOut"), total.get("weedOut")));
            }
            if (!Objects.equals(map.get("sale"), "-")) {
                total.put("sale", add(map.get("sale"), total.get("sale")));
            }
        }
        return maps;
    }


    private static String div(String total, String qty) {
        return new BigDecimal(total).divide(new BigDecimal(qty), 2, BigDecimal.ROUND_HALF_UP).toString();
    }

    private static String add(String qty1, String qty2) {
        Integer qty = EventUtil.plusInt(Integer.parseInt(qty1), Integer.parseInt(qty2));
        return qty.toString();
    }

    private static String multi(String qty1, String qty2) {
        return new BigDecimal(qty1).multiply(new BigDecimal(qty2)).toString();
    }
}
