package com.sky.mapper;

import com.sky.entity.SalesTopDish;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface ReportMapper {

    Long turnoverStatistics(LocalDate start, LocalDate end);
    @Select("select count(*) from user where create_time<#{begin}")
    Long getBeforeTime(LocalDate begin);
    @Select("select count(*) from orders where (checkout_time between #{begin} and #{end}) and status=5")
    Long validOrdersStatistics(LocalDate begin, LocalDate end);
    @Select("select count(*) from orders where (checkout_time between #{begin} and #{end}) ")
    Long ordersStatistics(LocalDate begin, LocalDate end);

    List<SalesTopDish> getTop(LocalDate begin, LocalDate end);
}
