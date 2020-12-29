package com.aniamadej.sublokator.controller;

import com.aniamadej.sublokator.dto.input.ReadingForm;
import com.aniamadej.sublokator.service.MediumMeterService;
import com.aniamadej.sublokator.util.Attributes;
import com.aniamadej.sublokator.util.Mappings;
import com.aniamadej.sublokator.util.Views;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
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
public class MetersController {

  private final MediumMeterService mediumMeterService;
  private final MessageSource errorsMessageSource;

  @Autowired
  MetersController(
      MediumMeterService mediumMeterService,
      MessageSource errorsMessageSource) {
    this.mediumMeterService = mediumMeterService;
    this.errorsMessageSource = errorsMessageSource;
  }


  @GetMapping(Mappings.METER_PAGE + "/{meterId}")
  public String showMediumMeter(@PathVariable("meterId") long meterId,
                                Model model) {
    if (!model.containsAttribute(Attributes.READING_FORM)) {
      model.addAttribute(Attributes.READING_FORM, new ReadingForm());
    }

    model.addAttribute(model.addAttribute(Attributes.MEDIUM_METER,
        mediumMeterService.findById(meterId)));
    return Views.METER;
  }

  @PostMapping(
      Mappings.METER_PAGE + "/{meterId}" + Mappings.READING_ADD_SUBPAGE)
  public String addNewReading(@PathVariable("meterId") Long meterId,
                              @ModelAttribute(Attributes.READING_FORM)
                              @Valid ReadingForm readingForm,
                              BindingResult bindingResult,
                              RedirectAttributes redirectAttributes) {

    if (bindingResult.hasErrors()) {
      redirectAttributes.addFlashAttribute(
          "org.springframework.validation.BindingResult."
              + Attributes.READING_FORM,
          bindingResult);
      redirectAttributes
          .addFlashAttribute(Attributes.READING_FORM, readingForm);
    } else {
      try {
        mediumMeterService.addReading(meterId, readingForm);
      } catch (Exception e) {
        redirectAttributes.addFlashAttribute(Attributes.ERROR, e.getMessage());
      }
    }
    return "redirect:" + Mappings.METER_PAGE + "/" + meterId;
  }

  @PostMapping(Mappings.METER_PAGE + "/{meterId}" + Mappings.DEACTIVATE)
  public String deactivateMeter(@PathVariable("meterId") Long meterId,
                                @RequestParam(Attributes.ACTIVE_UNTIL)
                                    String deactivationDate,
                                RedirectAttributes redirectAttributes) {
    try {
      mediumMeterService.deactivate(meterId, deactivationDate);
    } catch (IllegalArgumentException e) {
      redirectAttributes.addFlashAttribute(Attributes.ERROR, e.getMessage());
    }
    return "redirect:" + Mappings.METER_PAGE + "/" + meterId;
  }

  @PostMapping(Mappings.METER_PAGE + "/{meterId}" + Mappings.REACTIVATE)
  public String reactivateMeter(@PathVariable("meterId") Long meterId) {
    mediumMeterService.reactivate(meterId);
    return "redirect:" + Mappings.METER_PAGE + "/" + meterId;
  }

  @PostMapping(Mappings.METER_PAGE + "/{meterId}" + Mappings.RESET)
  public String resetMeter(@PathVariable("meterId") Long meterId,
                           @RequestParam(Attributes.RESET_DATE)
                               String deactivationDate,
                           RedirectAttributes redirectAttributes) {
    try {
      mediumMeterService.reset(meterId, deactivationDate);
    } catch (IllegalArgumentException e) {
      redirectAttributes.addFlashAttribute(Attributes.ERROR, e.getMessage());
    }
    return "redirect:" + Mappings.METER_PAGE + "/" + meterId;
  }

}
