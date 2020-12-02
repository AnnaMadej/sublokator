package com.aniamadej.sublokator.controller;

import com.aniamadej.sublokator.model.MediumConnection;
import com.aniamadej.sublokator.service.MediumConnectionService;
import com.aniamadej.sublokator.util.Attributes;
import com.aniamadej.sublokator.util.Mappings;
import com.aniamadej.sublokator.util.Views;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class MediaController {

    private MediumConnectionService mediumConnectionService;

    @Autowired
    public MediaController(MediumConnectionService mediumConnectionService) {
        this.mediumConnectionService = mediumConnectionService;
    }

    @GetMapping(Mappings.MEDIA_PAGE)
    public String showMediaConnections(Model model){
        model.addAttribute(Attributes.NAMES, mediumConnectionService.getNamesList());
        model.addAttribute(Attributes.REDIRECT_PAGE, Mappings.MEDIUM_PAGE);
        return Views.MEDIA_CONNECTIONS;
    }

    @GetMapping(Mappings.MEDIUM_PAGE + "/{mediumId}")
    public String showMedumConnection(Model model, @PathVariable long mediumId, @RequestParam(required = false) boolean inactive){
        model.addAttribute(Attributes.NAMES, mediumConnectionService.getMeterNumbers(mediumId, inactive));
        model.addAttribute(Attributes.REDIRECT_PAGE, Mappings.METER_PAGE);
        model.addAttribute(Attributes.MEDIUM_NAME, mediumConnectionService.getMediumName(mediumId));
        return Views.MEDIUM_METERS;
    }

    @GetMapping(Mappings.MEDIUM_ADD)
    public String addNewMedium(){
        return Views.ADD_MEDIUM;
    }

    @PostMapping(Mappings.MEDIUM_ADD)
    public String addNewMedium(@RequestParam(name= Attributes.MEDIUM_NAME) String mediumName){
        MediumConnection connection = new MediumConnection();
        connection.setMediumName(mediumName);
        mediumConnectionService.save(connection);
        return "redirect:" + Mappings.MEDIA_PAGE;
    }
}
