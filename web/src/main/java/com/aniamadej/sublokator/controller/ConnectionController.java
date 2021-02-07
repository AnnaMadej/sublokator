package com.aniamadej.sublokator.controller;

import com.aniamadej.sublokator.dto.input.MediumMeterForm;
import com.aniamadej.sublokator.service.MediumConnectionService;
import com.aniamadej.sublokator.util.Attributes;
import com.aniamadej.sublokator.util.Mappings;
import com.aniamadej.sublokator.util.Views;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping(Mappings.CONNECTION_PAGE)
public class ConnectionController {

  private final MediumConnectionService mediumConnectionService;

  @Autowired
  ConnectionController(
      MediumConnectionService mediumConnectionService) {
    this.mediumConnectionService = mediumConnectionService;
  }


  @GetMapping("/{mediumId}")
  public String showMediumConnection(Model model, @PathVariable long mediumId) {
    model.addAttribute(Attributes.MEDIUM_NAME,
        mediumConnectionService.getMediumName(mediumId));
    return Views.CONNECTION;
  }

  @GetMapping("/{mediumId}" + Mappings.METERS_SUBPAGE)
  public String showMediumMeters(Model model, @PathVariable long mediumId,
                                 @RequestParam(defaultValue = "false")
                                     boolean inactive,
                                 Pageable pageable) {

    model.addAttribute(Attributes.NAMES,
        mediumConnectionService.getMeterNumbers(mediumId, pageable, inactive));
    model.addAttribute(Attributes.REDIRECT_PAGE, Mappings.METER_PAGE);
    model.addAttribute(Attributes.MEDIUM_NAME,
        mediumConnectionService.getMediumName(mediumId));
    return Views.MEDIUM_METERS;
  }

  @GetMapping(
      "/{mediumId}" + Mappings.METERS_ADD_SUBPAGE)
  public String addNewMediumMeter(@PathVariable Long mediumId, Model model) {
    model.addAttribute(Attributes.MEDIUM_METER_FORM, new MediumMeterForm());
    return Views.METER_ADD;
  }


  @PostMapping(
      "/{mediumId}" + Mappings.METERS_ADD_SUBPAGE)
  public String addNewMediumMeter(@PathVariable Long mediumId,
                                  @ModelAttribute("mediumMeterForm")
                                  @Valid MediumMeterForm mediumMeterForm,
                                  BindingResult bindingResult) {

    if (bindingResult.hasErrors()) {
      return Views.METER_ADD;
    }
    mediumConnectionService.addMediumMeter(mediumId, mediumMeterForm);

    return "redirect:" + Mappings.CONNECTION_PAGE + "/" + mediumId
        + Mappings.METERS_SUBPAGE;
  }

}
