package com.aniamadej.sublokator.controller;

import com.aniamadej.sublokator.dto.input.MediumMeterForm;
import com.aniamadej.sublokator.service.MediumConnectionService;
import com.aniamadej.sublokator.service.MediumMeterService;
import com.aniamadej.sublokator.util.Attributes;
import com.aniamadej.sublokator.util.Mappings;
import com.aniamadej.sublokator.util.Views;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class MediaController {

  private final MediumConnectionService mediumConnectionService;

  @Autowired
  MediaController(MediumConnectionService mediumConnectionService,
                  MediumMeterService mediumMeterService,
                  ResourceBundleMessageSource messagesource) {
    this.mediumConnectionService = mediumConnectionService;
  }

  @GetMapping(Mappings.MEDIA_PAGE)
  public String showMediaConnections(Model model,
                                     @ModelAttribute("error") String error) {
    model
        .addAttribute(Attributes.NAMES, mediumConnectionService.getNamesList());
    model.addAttribute(Attributes.REDIRECT_PAGE, Mappings.MEDIUM_PAGE);
    return Views.MEDIA_CONNECTIONS;
  }

  @GetMapping(Mappings.MEDIUM_PAGE + "/{mediumId}" + Mappings.METERS_SUBPAGE)
  public String showMedumMeters(Model model, @PathVariable long mediumId,
                                @RequestParam(required = false)
                                    boolean inactive,
                                Pageable pageable,
                                RedirectAttributes redirectAttributes) {
    if (!mediumConnectionService.existsById(mediumId)) {
      return ControllersHelper
          .redirectToMainPageWithErrorMessageCode(redirectAttributes,
              "error.connectionNotExists");
    }
    model.addAttribute(Attributes.NAMES,
        mediumConnectionService.getMeterNumbers(mediumId, pageable));
    model.addAttribute(Attributes.REDIRECT_PAGE, Mappings.METER_PAGE);
    model.addAttribute(Attributes.MEDIUM_NAME,
        mediumConnectionService.getMediumName(mediumId));
    return Views.MEDIUM_METERS;
  }

  @GetMapping(Mappings.MEDIUM_PAGE + "/{mediumId}")
  public String showMedumConnection(Model model, @PathVariable long mediumId,
                                    RedirectAttributes redirectAttributes) {
    if (!mediumConnectionService.existsById(mediumId)) {
      return ControllersHelper
          .redirectToMainPageWithErrorMessageCode(redirectAttributes,
              "error.connectionNotExists");
    }
    model.addAttribute(Attributes.MEDIUM_NAME,
        mediumConnectionService.getMediumName(mediumId));
    return Views.MEDIUM;
  }

  @GetMapping(Mappings.MEDIA_ADD)
  public String addNewMedium() {
    return Views.ADD_MEDIUM;
  }

  @PostMapping(Mappings.MEDIA_ADD)
  public String addNewMedium(
      @RequestParam(name = Attributes.MEDIUM_NAME)
      @NotBlank @Valid String mediumName) {
    mediumConnectionService.save(mediumName);
    return "redirect:" + Mappings.MEDIA_PAGE;
  }

  @GetMapping(
      Mappings.MEDIUM_PAGE + "/{mediumId}" + Mappings.METERS_ADD_SUBPAGE)
  public String addNewMediumMeter(@PathVariable Long mediumId, Model model) {
    model.addAttribute(Attributes.MEDIUM_METER_FORM, new MediumMeterForm());
    return Views.METER_ADD;
  }


  @PostMapping(
      Mappings.MEDIUM_PAGE + "/{mediumId}" + Mappings.METERS_ADD_SUBPAGE)
  public String addNewMediumMeter(@PathVariable Long mediumId,
                                  @ModelAttribute("mediumMeterForm")
                                  @Valid MediumMeterForm mediumMeterForm,
                                  BindingResult bindingResult,
                                  RedirectAttributes ra) {

    if (bindingResult.hasErrors()) {
      return Views.METER_ADD;
    }
    try {
      mediumConnectionService.addMediumMeter(mediumId, mediumMeterForm);
    } catch (Exception e) {
      return ControllersHelper
          .redirectToMainPageWithErrorMessageCode(ra, e.getMessage());
    }

    return "redirect:" + Mappings.MEDIUM_PAGE + "/" + mediumId
        + Mappings.METERS_SUBPAGE;
  }

}
