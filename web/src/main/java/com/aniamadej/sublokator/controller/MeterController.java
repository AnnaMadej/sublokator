package com.aniamadej.sublokator.controller;

import com.aniamadej.sublokator.dto.input.ReadingForm;
import com.aniamadej.sublokator.service.MediumMeterService;
import com.aniamadej.sublokator.util.Attributes;
import com.aniamadej.sublokator.util.Mappings;
import com.aniamadej.sublokator.util.Views;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping(Mappings.METER_PAGE)
public class MeterController {

  private final MediumMeterService mediumMeterService;

  @Autowired
  MeterController(
      MediumMeterService mediumMeterService) {
    this.mediumMeterService = mediumMeterService;
  }


  @GetMapping("/{meterId}")
  public String showMediumMeter(@PathVariable("meterId") long meterId,
                                Model model) {
    if (!model.containsAttribute(Attributes.READING_FORM)) {
      model.addAttribute(Attributes.READING_FORM, new ReadingForm());
    }

    model.addAttribute(model.addAttribute(Attributes.MEDIUM_METER,
        mediumMeterService.findById(meterId)));
    return Views.METER;
  }

  @PostMapping("/{meterId}" + Mappings.READING_ADD_SUBPAGE)
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
      mediumMeterService.addReading(meterId, readingForm);
    }
    return "redirect:" + Mappings.METER_PAGE + "/" + meterId;
  }

  @PostMapping("/{meterId}" + Mappings.DEACTIVATE)
  public String deactivateMeter(@PathVariable("meterId") Long meterId,
                                @RequestParam(Attributes.ACTIVE_UNTIL)
                                    String deactivationDate) {
    mediumMeterService.deactivate(meterId, deactivationDate);

    return "redirect:" + Mappings.METER_PAGE + "/" + meterId;
  }

  @PostMapping("/{meterId}" + Mappings.REACTIVATE)
  public String reactivateMeter(@PathVariable("meterId") Long meterId) {
    mediumMeterService.reactivate(meterId);
    return "redirect:" + Mappings.METER_PAGE + "/" + meterId;
  }

  @PostMapping("/{meterId}" + Mappings.RESET)
  public String resetMeter(@PathVariable("meterId") Long meterId,
                           @RequestParam(Attributes.RESET_DATE)
                               String deactivationDate) {
    mediumMeterService.reset(meterId, deactivationDate);
    return "redirect:" + Mappings.METER_PAGE + "/" + meterId;
  }

}
