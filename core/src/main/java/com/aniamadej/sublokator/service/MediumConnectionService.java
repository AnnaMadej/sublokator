package com.aniamadej.sublokator.service;

import com.aniamadej.sublokator.dto.NameDto;
import com.aniamadej.sublokator.model.MediumConnection;
import com.aniamadej.sublokator.repository.MediumConnectionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MediumConnectionService {

    private MediumConnectionRepository mediumConnectionRepository;

    @Autowired
    public MediumConnectionService(MediumConnectionRepository mediumConnectionRepository) {
        this.mediumConnectionRepository = mediumConnectionRepository;
    }

    public MediumConnection save(MediumConnection mediumConnection){
        return mediumConnectionRepository.save(mediumConnection);
    }

    public List<NameDto> getNamesList(){
        return mediumConnectionRepository.fetchMediaNames();
    }

    public List<NameDto> getMeterNumbers(long mediumConnectionId, boolean inactive){
            return mediumConnectionRepository.fetchMeterNumbers(mediumConnectionId, !inactive);
    }

    public String getMediumName(long mediumConnectionId){
        return mediumConnectionRepository.fetchMediumName(mediumConnectionId);
    }
}
