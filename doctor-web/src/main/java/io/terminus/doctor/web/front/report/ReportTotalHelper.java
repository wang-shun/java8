package io.terminus.doctor.web.front.report;

import io.terminus.doctor.common.enums.IsOrNot;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author xjn
 * @date 18/5/28
 * email xiaojiannan@terminus.io
 */
 class ReportTotalHelper {

     static List<Map<String, String>> totalBoar(List<Map<String, String>> maps, Integer isNecessaryTotal){
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


     static List<Map<String, String>> totalDeliver(List<Map<String, String>> maps, Integer isNecessaryTotal){
        if (!Objects.equals(isNecessaryTotal, IsOrNot.YES.getKey())) {
            return maps;
        }

        Map<String, String> total = maps.get(maps.size() - 1);
        for (int i = 0; i < maps.size() -1 ; i++) {
            Map<String, String> map = maps.get(i);
            total.put("sowPhWeanOut", add(map.get("sowPhWeanOut"), total.get("sowPhWeanOut")));
            total.put("farrowNest", add(map.get("farrowNest"), total.get("farrowNest")));
            total.put("toNursery", add(map.get("toNursery"), total.get("toNursery")));
            total.put("weanNest", add(map.get("weanNest"), total.get("weanNest")));
            total.put("weanWeightPerFarrow", add(multi(map.get("weanWeightPerFarrow"), map.get("weanCount")), total.get("weanWeightPerFarrow")));
            total.put("weanDayAge", add(multi(map.get("weanDayAge"), map.get("weanCount")), total.get("weanDayAge")));
            total.put("pigletSale", add(map.get("pigletSale"), total.get("pigletSale")));
            total.put("firstBornWeight", add(multi(map.get("firstBornWeight"), map.get("farrowLiving")), total.get("firstBornWeight")));
            total.put("pigletChgFarmOutAvgWeight", add(multi(map.get("pigletChgFarmOutAvgWeight"), map.get("pigletChgFarmOut")), total.get("pigletChgFarmOutAvgWeight")));
            total.put("pigletSaleAveWeight", add(multi(map.get("pigletSaleAveWeight"), map.get("pigletSale")), total.get("pigletSaleAveWeight")));
        }

        String farrowNest = total.get("farrowNest");
        total.put("pigletCountPerFarrow", div(total.get("farrowAll"), farrowNest));
        total.put("pigletLivingCountPerFarrow", div(total.get("farrowLiving"), farrowNest));
        total.put("pigletHealthCountPerFarrow", div(total.get("farrowHealth"), farrowNest));
        total.put("pigletWeakCountPerFarrow", div(total.get("farrowWeak"), farrowNest));
        total.put("avgWeightPerFarrow", div(total.get("firstBornWeight"), farrowNest));
        total.put("firstBornWeight", div(total.get("firstBornWeight"), total.get("farrowLiving")));

        total.put("weanCountPerFarrow", div(total.get("weanCountPerFarrow"), total.get("weanNest")));
        total.put("weanWeightPerFarrow", div(total.get("weanWeightPerFarrow"), total.get("weanCount")));
        total.put("weanDayAge", div(total.get("weanDayAge"), total.get("weanCount")));

        total.put("pigletChgFarmOutAvgWeight", div(total.get("pigletChgFarmOutAvgWeight"), total.get("pigletChgFarmOut")));
        total.put("pigletSaleAveWeight", div(total.get("pigletSaleAveWeight"), total.get("pigletSale")));

        return maps;
    }

     static List<Map<String, String>> totalFatten(List<Map<String, String>> maps, Integer isNecessaryTotal){
        if (!Objects.equals(isNecessaryTotal, IsOrNot.YES.getKey())) {
            return maps;
        }

        Map<String, String> total = maps.get(maps.size() - 1);
        for (int i = 0; i < maps.size() -1 ; i++) {
            Map<String, String> map = maps.get(i);
            total.put("turnInto", add(map.get("turnInto"), total.get("turnInto")));
            total.put("turnIntoAvgWeight", add(multi(map.get("turnIntoAvgWeight"), map.get("turnInto")), total.get("turnIntoAvgWeight")));
            total.put("turnIntoAge", add(multi(map.get("turnIntoAge"), map.get("turnInto")), total.get("turnIntoAge")));
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
        total.put("turnIntoAge", div(total.get("turnIntoAge"), total.get("turnInto")));
        total.put("saleAvgWeight", div(total.get("saleAvgWeight"), total.get("sale")));
        total.put("toHoubeiAvgWeight", div(total.get("toHoubeiAvgWeight"), total.get("toHoubei")));
        total.put("chgFarmAvgWeight", div(total.get("chgFarmAvgWeight"), total.get("chgFarmOut")));

        return maps;
    }

     static List<Map<String, String>> totalMating(List<Map<String, String>> maps, Integer isNecessaryTotal) {
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

     static List<Map<String, String>> totalNursery(List<Map<String, String>> maps, Integer isNecessaryTotal) {
        if (!Objects.equals(isNecessaryTotal, IsOrNot.YES.getKey())) {
            return maps;
        }

        Map<String, String> total = maps.get(maps.size() - 1);
        for (int i = 0; i < maps.size() - 1; i++) {
            Map<String, String> map = maps.get(i);
            total.put("turnInto", add(map.get("turnInto"), total.get("turnInto")));
            total.put("turnIntoAvgWeight", add(multi(map.get("turnIntoAvgWeight"), map.get("turnInto")), total.get("turnIntoAvgWeight")));
            total.put("turnIntoAge", add(multi(map.get("turnIntoAge"), map.get("turnInto")), total.get("turnIntoAge")));
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
        total.put("turnIntoAge", div(total.get("turnIntoAge"), total.get("turnInto")));
        total.put("saleAvgWeight", div(total.get("saleAvgWeight"), total.get("sale")));
        total.put("toFattenAvgWeight", div(total.get("toFattenAvgWeight"), total.get("toFatten")));
        total.put("toHoubeiAvgWeight", div(total.get("toHoubeiAvgWeight"), total.get("toHoubei")));
        total.put("chgFarmAvgWeight", div(total.get("chgFarmAvgWeight"), total.get("chgFarmOut")));

        return maps;
    }

     static List<Map<String, String>> totalReserve(List<Map<String, String>> maps, Integer isNecessaryTotal) {
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

     static List<Map<String, String>> totalSow(List<Map<String, String>> maps, Integer isNecessaryTotal) {
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
        if (Objects.equals(total, "-")) {
            total = "0";
        }
        if (Objects.equals(qty, "-")) {
            return "0";
        }
        BigDecimal bigDecimal = new BigDecimal(qty);
        if (bigDecimal.doubleValue() == 0.0) {
            return "0";
        }
        return new BigDecimal(total).divide(new BigDecimal(qty), 2, BigDecimal.ROUND_HALF_UP).toString();
    }

    private static String add(String qty1, String qty2) {
        if (Objects.equals(qty1, "-")) {
            qty1 = "0";
        }
        if (Objects.equals(qty2, "-")) {
            qty2 = "0";
        }

        return new BigDecimal(qty1).add(new BigDecimal(qty2)).toString();
    }

    private static String multi(String qty1, String qty2) {
        if (Objects.equals(qty1, "-")) {
            qty1 = "0";
        }
        if (Objects.equals(qty2, "-")) {
            qty2 = "0";
        }
        return new BigDecimal(qty1).multiply(new BigDecimal(qty2)).toString();
    }
}
