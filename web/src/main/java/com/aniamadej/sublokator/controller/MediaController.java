package com.aniamadej.sublokator.controller;

import com.aniamadej.sublokator.service.MediumConnectionService;
import com.aniamadej.sublokator.util.Attributes;
import com.aniamadej.sublokator.util.Mappings;
import com.aniamadej.sublokator.util.Views;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class MediaController {

    private final MediumConnectionService mediumConnectionService;

    @Autowired
    MediaController(MediumConnectionService mediumConnectionService) {
        this.mediumConnectionService = mediumConnectionService;
    }


    @GetMapping(Mappings.MEDIA_PAGE)
    public String showMediaConnections(Model model){
        model.addAttribute(Attributes.NAMES, mediumConnectionService.getNamesList());
        model.addAttribute(Attributes.REDIRECT_PAGE, Mappings.MEDIUM_PAGE);
        return Views.MEDIA_CONNECTIONS;
    }

    @GetMapping(Mappings.MEDIUM_PAGE + "/{mediumId}" + Mappings.METERS_SUBPAGE)
    public String showMedumMeters(Model model, @PathVariable long mediumId, @RequestParam(required = false) boolean inactive, Pageable pageable){
        if(!mediumConnectionService.existsById(mediumId)){
            return "redirect:/" + Mappings.MEDIA_PAGE;
        }
        model.addAttribute(Attributes.NAMES, mediumConnectionService.getMeterNumbers(mediumId, inactive, pageable));
        model.addAttribute(Attributes.REDIRECT_PAGE, Mappings.METER_PAGE);
        model.addAttribute(Attributes.MEDIUM_NAME, mediumConnectionService.getMediumName(mediumId));
        return Views.MEDIUM_METERS;
    }

    @GetMapping(Mappings.MEDIUM_PAGE + "/{mediumId}")
    public String showMedumConnection(Model model, @PathVariable long mediumId){
        if(!mediumConnectionService.existsById(mediumId)){
            return "redirect:/" + Mappings.MEDIA_PAGE;
        }
        model.addAttribute(Attributes.MEDIUM_NAME, mediumConnectionService.getMediumName(mediumId));
        return Views.MEDIUM;
    }

    @GetMapping(Mappings.MEDIA_PAGE + Mappings.ADD)
    public String addNewMedium(){
        return Views.ADD_MEDIUM;
    }


    @PostMapping(Mappings.MEDIA_PAGE + Mappings.ADD)
    public String addNewMedium(@RequestParam(name= Attributes.MEDIUM_NAME) String mediumName){
        mediumConnectionService.save(mediumName);
        return "redirect:" + Mappings.MEDIA_PAGE;
    }

    @GetMapping(Mappings.MEDIUM_PAGE + "/{mediumId}" + Mappings.METERS_SUBPAGE + Mappings.ADD)
    public String addNewMediumMeter(@PathVariable Long mediumId, Model model){
        return Views.METER_ADD;
    }

}
