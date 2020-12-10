package com.aniamadej.sublokator.service;

import com.aniamadej.sublokator.dto.MediumMeterForm;
import com.aniamadej.sublokator.dto.NameDto;
import com.aniamadej.sublokator.model.MediumConnection;
import com.aniamadej.sublokator.model.MediumMeter;
import com.aniamadej.sublokator.repository.MediumConnectionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class MediumConnectionService {

    // == fields ==
    private final MediumConnectionRepository mediumConnectionRepository;

    // == constructors ==
    @Autowired
    public MediumConnectionService(MediumConnectionRepository mediumConnectionRepository) {
        this.mediumConnectionRepository = mediumConnectionRepository;
    }


    // == public methods ==
    public List<NameDto> getNamesList(){
        return mediumConnectionRepository.findMediaNames();
    }

    public List<NameDto> getMeterNumbers(long mediumConnectionId, boolean inactive, Pageable pageable){
            return mediumConnectionRepository.fetchMeterNumbers(mediumConnectionId, !inactive, pageable).getContent();
    }

    public String getMediumName(long mediumConnectionId){
        return mediumConnectionRepository.findMediumName(mediumConnectionId).orElse("");
    }

    public boolean existsById(Long mediumConnectionId){
        return mediumConnectionRepository.existsById(mediumConnectionId);
    }

    public void save(String name){
        MediumConnection connection = new MediumConnection();
        connection.setMediumName(name);
        mediumConnectionRepository.save(connection);
    }
    // TODO: change error message to international
    public void addMediumMeter(Long mediumConnectionId, MediumMeterForm mediumMeterForm){
        MediumConnection mediumConnection = mediumConnectionRepository.findById(mediumConnectionId)
                .map(connection -> {
                    MediumMeter mediumMeter = mediumMeterForm.toMediumMeter();
                    connection.addMediumMeter(mediumMeter);
                    return mediumConnectionRepository.save(connection);
                }).orElseThrow(() -> new IllegalArgumentException("no mediumConnection of this id"));
    }
}