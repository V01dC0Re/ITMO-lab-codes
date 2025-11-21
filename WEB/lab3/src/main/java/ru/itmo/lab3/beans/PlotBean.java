package ru.itmo.lab3.beans;

import java.util.stream.Collectors;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import ru.itmo.lab3.model.HitResult;

@Named
@SessionScoped
public class PlotBean implements Serializable {
    private static final long serialVersionUID = 1L;

    @Inject
    private ResultsBean resultsBean;

    private List<SelectableX> xOptions = new ArrayList<>();
    private BigDecimal y;
    private BigDecimal r = new BigDecimal("2.0");
    private String plotDataJson;

    @PostConstruct
    public void init() {
        initXOptions();
        loadPlotData();
    }

    private void initXOptions() {
        xOptions.clear();
        for (int i = -5; i <= 1; i++) {
            xOptions.add(new SelectableX(BigDecimal.valueOf(i), false));
        }
    }

    public void loadPlotData() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            PlotData data = new PlotData();
            data.r = getR().doubleValue();
            System.out.println(r.doubleValue());
            System.out.println();
            data.points = new ArrayList<>();
    
            List<HitResult> allResults = resultsBean.getAllResults();
            System.out.println("кол-во результатов для графика: " + allResults.size());
            
            for (HitResult res : allResults) {
                PointData point = new PointData();
                point.x = res.getX().doubleValue();
                point.y = res.getY().doubleValue();

                point.hit = isPointInArea(res.getX(), res.getY(), this.r);
                
                System.out.printf("точка: X=%.2f, Y=%.2f, R=%.1f, Hit=%b%n",
                    point.x, point.y, r.doubleValue(), point.hit);
                
                data.points.add(point);
            }
    
            plotDataJson = mapper.writeValueAsString(data);
            System.out.println("JSON для графика: " + plotDataJson);
        } catch (Exception e) {
            System.err.println("ошибка генерации JSON: " + e.getMessage());
            e.printStackTrace();
            plotDataJson = "{\"r\":2.0,\"points\":[]}";
        }
    }

    public String getPlotDataJson() {
        if (plotDataJson == null || plotDataJson.isEmpty()) {
            loadPlotData();
        }
        return plotDataJson;
    }

private boolean validateManualInput() {
    FacesContext context = FacesContext.getCurrentInstance();
    boolean valid = true;

    if (y == null || 
        y.compareTo(BigDecimal.valueOf(-3)) <= 0 || 
        y.compareTo(BigDecimal.valueOf(3)) >= 0) {
        context.addMessage("mainForm:y", new FacesMessage(
            FacesMessage.SEVERITY_ERROR, "Ошибка", "y  -3 до 3"));
        valid = false;
    }

    if (r == null || 
        r.compareTo(BigDecimal.valueOf(2)) < 0 || 
        r.compareTo(BigDecimal.valueOf(5)) > 0) {
        context.addMessage("mainForm:r", new FacesMessage(
            FacesMessage.SEVERITY_ERROR, "Ошибка", "R от 2 до 5"));
        valid = false;
    }

    return valid;
}

public void checkHit() {
    if (validateManualInput()) {
        try {
            List<BigDecimal> selectedXValues = xOptions.stream()
                .filter(opt -> Boolean.TRUE.equals(opt.selected))
                .map(opt -> opt.value)
                .collect(Collectors.toList());

            if (selectedXValues.isEmpty()) {
                FacesContext.getCurrentInstance().addMessage("mainForm:xOptionsGroup", 
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Ошибка", "выберите хотя бы одно X"));
                return;
            }

            StringBuilder resultsSummary = new StringBuilder();
            for (BigDecimal xValue : selectedXValues) {
                boolean hit = isPointInArea(xValue, y, r);
                resultsBean.saveResult(new HitResult(xValue, y, r, hit));
                
                resultsSummary.append(String.format("X=%s, Y=%s → %s\n",
                    xValue.setScale(2, RoundingMode.HALF_UP),
                    y.setScale(2, RoundingMode.HALF_UP),
                    hit ? "попадание" : "промах"));
            }

            loadPlotData();
            
            FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage(null, new FacesMessage(
                FacesMessage.SEVERITY_INFO,
                "Результаты",
                resultsSummary.toString()
            ));
        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                FacesMessage.SEVERITY_ERROR,
                "Ошибка сохранения",
                "Данные не сохранены: " + e.getMessage()
            ));
        }
    }
}

public void handleCanvasClick() {
    FacesContext context = FacesContext.getCurrentInstance();
    Map<String, String> params = context.getExternalContext().getRequestParameterMap();

    String rParam = params.get("r");
    if (rParam != null) {
        try {
            BigDecimal newR = new BigDecimal(rParam);
            if (newR.compareTo(BigDecimal.valueOf(2)) >= 0 && 
                newR.compareTo(BigDecimal.valueOf(5)) <= 0) {
                this.r = newR.setScale(1, RoundingMode.HALF_UP);
            }
        } catch (NumberFormatException e) {
        }
    }

    String xParam = params.get("x");
    String yParam = params.get("y");

    if (xParam == null || yParam == null) {
        context.addMessage(null, new FacesMessage(
            FacesMessage.SEVERITY_ERROR, "Ошибка", "Нет координат"));
        return;
    }

    try {
        BigDecimal x = new BigDecimal(xParam).setScale(2, RoundingMode.HALF_UP);
        BigDecimal y = new BigDecimal(yParam).setScale(2, RoundingMode.HALF_UP);



        boolean hit = isPointInArea(x, y, this.r);
        resultsBean.saveResult(new HitResult(x, y, this.r, hit));
        loadPlotData();

        context.addMessage(null, new FacesMessage(
            FacesMessage.SEVERITY_INFO,
            "Успех",
            "Клик: (" + x + ", " + y + ") при R=" + this.r + " → " + 
            (hit ? "попадание" : "промах")
        ));

    } catch (NumberFormatException e) {
        context.addMessage(null, new FacesMessage(
            FacesMessage.SEVERITY_ERROR, "Ошибка", "Некорректные координаты"));
    }
}

public void onRChanged() {
    System.out.println("R изменен на сервере: " + this.r);
    
    FacesContext context = FacesContext.getCurrentInstance();
    Iterator<FacesMessage> msgs = context.getMessages("mainForm:r");
    while (msgs.hasNext()) {
        msgs.next();
        msgs.remove();
    }
    
    loadPlotData();
}

    private boolean isPointInArea(BigDecimal x, BigDecimal y, BigDecimal r) {
        if (x == null || y == null) return false;

        if (x.compareTo(BigDecimal.ZERO) > 0 && y.compareTo(BigDecimal.ZERO) > 0) {
            return false;
        }
    
        boolean inSecond = false;
        boolean inThird = false;
        boolean inFourth = false;
    
        // 2-я четверть
        if (x.compareTo(BigDecimal.ZERO) <= 0 && y.compareTo(BigDecimal.ZERO) >= 0) {
            BigDecimal x2 = x.multiply(x);
            BigDecimal y2 = y.multiply(y);
            BigDecimal halfR = r.divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
            BigDecimal radiusSq = halfR.multiply(halfR);
            BigDecimal distSq = x2.add(y2);
            inSecond = distSq.compareTo(radiusSq) <= 0;
        }
    
        // 3-я четверть
        if (x.compareTo(BigDecimal.ZERO) <= 0 && y.compareTo(BigDecimal.ZERO) <= 0) {
            BigDecimal sum = x.add(y);
            inThird = sum.compareTo(r.negate()) >= 0;
        }
    
        // 4-я четверть
        if (x.compareTo(BigDecimal.ZERO) >= 0 && y.compareTo(BigDecimal.ZERO) <= 0) {
            boolean xIn = x.compareTo(r) <= 0;
            boolean yIn = y.compareTo(r.negate()) >= 0;
            inFourth = xIn && yIn;
        }
    
        return inSecond || inThird || inFourth;
    }

public static class SelectableX implements Serializable {
    private static final long serialVersionUID = 1L;

    private BigDecimal value;
    private boolean selected;

    public SelectableX() {}

    public SelectableX(BigDecimal value, boolean selected) {
        this.value = value;
        this.selected = selected;
    }


    public BigDecimal getValue() { return value; }
    public void setValue(BigDecimal value) { this.value = value; }  // ← ДОБАВИТЬ

    public boolean isSelected() { return selected; }
    public void setSelected(boolean selected) { this.selected = selected; }
}
    public static class PlotData {
        public double r;
        public List<PointData> points = new ArrayList<>();
    }

    public static class PointData {
        public double x;
        public double y;
        public boolean hit;
    }

    public List<SelectableX> getXOptions() { return xOptions; }
    public BigDecimal getY() { return y; }
    public void setY(BigDecimal y) { this.y = y; }
    public BigDecimal getR() { return r; }
    public void setR(BigDecimal r) { 
        System.out.println(r.toString());
        if (r != null && r.compareTo(BigDecimal.valueOf(2)) >= 0 
            && r.compareTo(BigDecimal.valueOf(5)) <= 0) {
            this.r = r.setScale(1, RoundingMode.HALF_UP);
        } else {
            this.r = new BigDecimal("2.0");
        }
    }
    public void setPlotDataJson(String json) {
    }
    public void getPlotDataJson(String json) {
    }
}