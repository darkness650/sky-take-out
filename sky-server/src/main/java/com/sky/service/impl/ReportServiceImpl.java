package com.sky.service.impl;

import com.sky.dto.DataOverViewQueryDTO;
import com.sky.entity.SalesTopDish;
import com.sky.mapper.ReportMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class ReportServiceImpl implements ReportService {
    @Autowired
    private ReportMapper reportMapper;
    @Autowired
    private WorkspaceService workspaceService;
    @Override
    public TurnoverReportVO turnoverStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dates = handleDate(begin, end);
        dates.add(dates.getLast().plusDays(1));
        List<Long> Statistics=new ArrayList<>();
        for(int i=0;i<dates.size()-1;i++) {
            Long num=reportMapper.turnoverStatistics(dates.get(i),dates.get(i+1));
            if(num!=null)Statistics.add(num);
            else Statistics.add(0L);
        }
        StringBuilder getDates = new StringBuilder();
        StringBuilder getStatistics = new StringBuilder();
        for(int i=0;i<dates.size()-1;i++) {
            getDates.append(dates.get(i));
            getStatistics.append(Statistics.get(i));
            if(i!=dates.size()-1) {getStatistics.append(",");getDates.append(",");}
        }
        return new TurnoverReportVO(getDates.toString(),getStatistics.toString());
    }

    @Override
    public UserReportVO userStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dates = handleDate(begin,end);
        Long startNumber=reportMapper.getBeforeTime(begin);
        List<Long> Statistics=new ArrayList<>();
        List<Long> newUsers=new ArrayList<>();
        for(int i=0;i<dates.size();i++) {
            Long num=reportMapper.getBeforeTime(dates.get(i));
            if(num==null) num=0L;
            Statistics.add(num);
            if(i==0) {newUsers.add(num-startNumber);}
            else {newUsers.add(num-Statistics.get(i-1));}
        }
        StringBuilder getDates = new StringBuilder();
        StringBuilder getStatistics = new StringBuilder();
        StringBuilder getNewUsers = new StringBuilder();
        for(int i=0;i<dates.size();i++) {
            getDates.append(dates.get(i));
            getStatistics.append(Statistics.get(i));
            getNewUsers.append(newUsers.get(i));
            if(i!=dates.size()-1) {getStatistics.append(",");getDates.append(",");getNewUsers.append(",");}
        }
        return new UserReportVO(dates.toString(),getStatistics.toString(),getNewUsers.toString());
    }

    @Override
    public OrderReportVO ordersStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dates = handleDate(begin,end);
        Long allValidOrder=reportMapper.validOrdersStatistics(begin,end.plusDays(1));
        Long allOrders=reportMapper.ordersStatistics(begin,end.plusDays(1));
        List<Long> dailyValidOrder=new ArrayList<>();
        List<Long> dailyOrders=new ArrayList<>();
        dates.add(dates.getLast().plusDays(1));
        for(int i=0;i<dates.size()-1;i++) {
            Long validOrder=reportMapper.validOrdersStatistics(dates.get(i),dates.get(i+1));
            Long orders=reportMapper.ordersStatistics(dates.get(i),dates.get(i+1));
            if(validOrder==null) validOrder=0L;
            if(orders==null) orders=0L;
            dailyValidOrder.add(validOrder);
            dailyOrders.add(orders);
        }
        StringBuilder getDates = new StringBuilder();
        StringBuilder getDailyValidOrder = new StringBuilder();
        StringBuilder getDailyOrders = new StringBuilder();
        for(int i=0;i<dates.size()-1;i++) {
            getDates.append(dates.get(i));
            getDailyValidOrder.append(dailyValidOrder.get(i));
            getDailyOrders.append(dailyOrders.get(i));
            if(i!=dates.size()-1) {getDailyOrders.append(",");getDates.append(",");getDailyValidOrder.append(",");}
        }
        Double orderCompletionRate=Double.parseDouble(allValidOrder.toString())/Double.parseDouble(allOrders.toString());
        return OrderReportVO.builder().orderCompletionRate(orderCompletionRate)
                .dateList(getDates.toString())
                .orderCountList(getDailyOrders.toString())
                .totalOrderCount(Math.toIntExact(allOrders))
                .validOrderCount(Math.toIntExact(allValidOrder))
                .validOrderCountList(getDailyValidOrder.toString())
                .build();
    }

    @Override
    public SalesTop10ReportVO top10(LocalDate begin, LocalDate end) {
        List<SalesTopDish> salesTopDishList=reportMapper.getTop(begin,end);
        StringBuilder getTop10Names = new StringBuilder();
        StringBuilder getTop10Count = new StringBuilder();
        for (SalesTopDish salesTopDish : salesTopDishList) {
            getTop10Names.append(salesTopDish.getName()).append(",");
            getTop10Count.append(salesTopDish.getCount()).append(",");
        }
        getTop10Count.deleteCharAt(getTop10Count.length()-1);
        getTop10Names.deleteCharAt(getTop10Names.length()-1);
        return new SalesTop10ReportVO(getTop10Names.toString(),getTop10Count.toString());
    }

    @Override
    public void exportBussinessData(HttpServletResponse response) {
        LocalDate date = LocalDate.now();
        LocalDate begin = date.minusDays(30);
        BusinessDataVO businessData = workspaceService.getBusinessData(date.minusDays(30).atStartOfDay(), date.atStartOfDay());
        InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");
        try {
            XSSFWorkbook excel = new XSSFWorkbook(resourceAsStream);
            XSSFSheet sheet1 = excel.getSheet("Sheet1");
            sheet1.getRow(1).getCell(1).setCellValue("时间："+date.minusDays(30)+"到"+date.minusDays(1));
            XSSFRow row = sheet1.getRow(3);
            row.getCell(2).setCellValue(businessData.getTurnover());
            row.getCell(4).setCellValue(businessData.getOrderCompletionRate());
            row.getCell(6).setCellValue(businessData.getNewUsers());
            row=sheet1.getRow(4);
            row.getCell(2).setCellValue(businessData.getValidOrderCount());
            row.getCell(4).setCellValue(businessData.getUnitPrice());
            for (int i = 0; i < 30; i++) {
                LocalDate datenow=begin.plusDays(i);
                BusinessDataVO businessData1 = workspaceService.getBusinessData(datenow.atStartOfDay(), datenow.plusDays(1).atStartOfDay());
                row = sheet1.getRow(7 + i);
                row.getCell(1).setCellValue(datenow.toString());
                row.getCell(2).setCellValue(businessData1.getTurnover());
                row.getCell(3).setCellValue(businessData1.getValidOrderCount());
                row.getCell(4).setCellValue(businessData1.getOrderCompletionRate());
                row.getCell(5).setCellValue(businessData1.getUnitPrice());
                row.getCell(6).setCellValue(businessData1.getNewUsers());
            }
            ServletOutputStream outputStream = response.getOutputStream();
            excel.write(outputStream);

            excel.close();
            outputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }finally {

        }


    }

    private ArrayList<LocalDate> handleDate(LocalDate begin, LocalDate end) {
        ArrayList<LocalDate> dates = new ArrayList<>();
        LocalDate now=begin;
        while(now.isBefore(end) || now.isEqual(end)) {
            dates.add(now);
            now=now.plusDays(1);
        }
        //dates.add(now);
        return dates;
    }
}
