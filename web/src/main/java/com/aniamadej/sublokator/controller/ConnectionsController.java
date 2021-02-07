package com.aniamadej.sublokator.controller;

import com.aniamadej.sublokator.service.MediumConnectionService;
import com.aniamadej.sublokator.service.MediumService;
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
@RequestMapping(Mappings.CONNECTIONS_PAGE)
public class ConnectionsController {

  private final MediumConnectionService mediumConnectionService;
  private final MediumService mediumService;

  @Autowired
  ConnectionsController(
      MediumConnectionService mediumConnectionService,
      MediumService mediumService) {
    this.mediumConnectionService = mediumConnectionService;
    this.mediumService = mediumService;
  }


  @GetMapping()
  public String showMediaConnections(Model model) {
    model
        .addAttribute(Attributes.NAMES, mediumConnectionService.getNamesList());
    model.addAttribute(Attributes.REDIRECT_PAGE, Mappings.CONNECTION_PAGE);
    return Views.MEDIA_CONNECTIONS;
  }

  @GetMapping(Mappings.ADD)
  public String addNewMedium(Model model) {
    model.addAttribute(Attributes.MEDIA, mediumService.getNamesList());
    return Views.ADD_CONNECTION;
  }

  @PostMapping(Mappings.ADD)
  public String addNewMedium(
      @RequestParam(name = Attributes.MEDIUM_NAME) String mediumName,
      @RequestParam(name = Attributes.DESCRIPTION) String description) {
    Long mediumId = mediumConnectionService.save(mediumName, description);
    return "redirect:" + Mappings.CONNECTION_PAGE + "/" + mediumId;
  }


}
