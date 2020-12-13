package com.aniamadej.sublokator.controller;

import com.aniamadej.sublokator.dto.input.ReadingForm;
import com.aniamadej.sublokator.service.MediumMeterService;
import com.aniamadej.sublokator.util.Attributes;
import com.aniamadej.sublokator.util.Mappings;
import com.aniamadej.sublokator.util.Views;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

@Controller
public class MetersController {

    private final MediumMeterService mediumMeterService;

    @Autowired
    MetersController(MediumMeterService mediumMeterService) {
        this.mediumMeterService = mediumMeterService;
    }

    @GetMapping(Mappings.METER_PAGE + "/{meterId}")
    public String showMediumMeter(@PathVariable("meterId") long meterId, Model model,
                                  RedirectAttributes redirectAttributes) {
        if (!model.containsAttribute(Attributes.READING_FORM)) {
            model.addAttribute(Attributes.READING_FORM, new ReadingForm());
        }
        return mediumMeterService.findById(meterId).map(meter -> {
            model.addAttribute("mediumMeter", meter);
            return Views.METER;
        }).orElseGet(() ->
                ControllersHelper.redirectToMainPageWithErrorMessageCode(redirectAttributes, "error.meterNotExists"));
    }

    @PostMapping(Mappings.METER_PAGE + "/{meterId}" + Mappings.READING_ADD_SUBPAGE)
    public String addNewReading(@PathVariable("meterId") Long meterId,
                                @ModelAttribute(Attributes.READING_FORM) @Valid ReadingForm readingForm,
                                BindingResult bindingResult, RedirectAttributes ra) {
        if (!mediumMeterService.existsById(meterId)) {
            return ControllersHelper.redirectToMainPageWithErrorMessageCode(ra,
                    "error.connectionNotExists");
        }
        if (bindingResult.hasErrors()) {
            ra.addFlashAttribute(
                    "org.springframework.validation.BindingResult." + Attributes.READING_FORM,
                    bindingResult);
            ra.addFlashAttribute(Attributes.READING_FORM, readingForm);
        } else {
            try {
                mediumMeterService.addReading(meterId, readingForm);
            } catch (Exception e) {
                ra.addFlashAttribute("error", e.getMessage());
            }
        }
        return "redirect:" + Mappings.METER_PAGE + "/{meterId}";
    }

}
