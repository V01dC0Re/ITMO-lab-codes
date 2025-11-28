package ru.itmo.lab3.model;

import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.FilterMeta;
import ru.itmo.lab3.beans.ResultsBean;

import java.util.List;
import java.util.Map;

public class ResultLazyModel extends LazyDataModel<HitResult> {
    private final ResultsBean resultsBean;

    public ResultLazyModel(ResultsBean resultsBean) {
        this.resultsBean = resultsBean;
    }

    @Override
    public List<HitResult> load(
        int first,
        int pageSize,
        Map<String, SortMeta> sortBy,
        Map<String, FilterMeta> filterBy
    ) {
        return resultsBean.getResultsPage(first, pageSize);
    }

    @Override
    public int count(Map<String, FilterMeta> filterBy) {
        return resultsBean.getTotalResultsCount();
    }
}