package com.pwr.project.services.search;

import com.pwr.project.entities.Notice;
import com.pwr.project.entities.search.SearchCriteriaRequest;
import com.pwr.project.repositories.NoticeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdvanceSearchService {

    @Autowired
    private NoticeRepository noticeRepository;

    public List<Notice> findBySearchCriteria(SearchCriteriaRequest searchCriteriaRequest) {
        Specification<Notice> searchSpecification = SearchSpecification
            .createSpecification(searchCriteriaRequest.getSearchCriteria());

        if (searchSpecification == null) {
            return noticeRepository.findAll();
        }

        return noticeRepository.findAll(searchSpecification);
    }
}
