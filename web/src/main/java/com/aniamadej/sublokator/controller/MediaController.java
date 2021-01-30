package com.aniamadej.sublokator.controller;

import com.aniamadej.sublokator.service.MediumConnectionService;
import com.aniamadej.sublokator.util.Attributes;
import com.aniamadej.sublokator.util.Mappings;
import com.aniamadej.sublokator.util.Views;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping(Mappings.MEDIA_PAGE)
public class MediaController {

  private final MediumConnectionService mediumConnectionService;

  @Autowired
  MediaController(
      MediumConnectionService mediumConnectionService) {
    this.mediumConnectionService = mediumConnectionService;
  }


  @GetMapping()
  public String showMediaConnections(Model model) {
    model
        .addAttribute(Attributes.NAMES, mediumConnectionService.getNamesList());
    model.addAttribute(Attributes.REDIRECT_PAGE, Mappings.MEDIUM_PAGE);
    return Views.MEDIA_CONNECTIONS;
  }

  @GetMapping(Mappings.ADD)
  public String addNewMedium() {
    return Views.ADD_MEDIUM;
  }

  @PostMapping(Mappings.ADD)
  public String addNewMedium(
      @RequestParam(name = Attributes.MEDIUM_NAME) String mediumName) {
    mediumConnectionService.save(mediumName);
    return "redirect:" + Mappings.MEDIA_PAGE;
  }


}
