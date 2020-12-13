package com.aniamadej.sublokator.controller;

import com.aniamadej.sublokator.dto.input.MediumMeterForm;
import com.aniamadej.sublokator.service.MediumConnectionService;
import com.aniamadej.sublokator.service.MediumMeterService;
import com.aniamadej.sublokator.util.Attributes;
import com.aniamadej.sublokator.util.Mappings;
import com.aniamadej.sublokator.util.Views;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

@Controller
public class MediaController {

    private final MediumConnectionService mediumConnectionService;
    private final MediumMeterService mediumMeterService;
    private final ResourceBundleMessageSource messagesource;

    @Autowired
    MediaController(MediumConnectionService mediumConnectionService,
                    MediumMeterService mediumMeterService,
                    ResourceBundleMessageSource messagesource) {
        this.mediumConnectionService = mediumConnectionService;
        this.mediumMeterService = mediumMeterService;
        this.messagesource = messagesource;
    }

    @GetMapping(Mappings.MEDIA_PAGE)
    public String showMediaConnections(Model model, @RequestParam(required = false) String error) {
        model.addAttribute(Attributes.NAMES, mediumConnectionService.getNamesList());
        model.addAttribute(Attributes.REDIRECT_PAGE, Mappings.MEDIUM_PAGE);
        model.addAttribute("error", error);
        return Views.MEDIA_CONNECTIONS;
    }

    @GetMapping(Mappings.MEDIUM_PAGE + "/{mediumId}" + Mappings.METERS_SUBPAGE)
    public String showMedumMeters(Model model, @PathVariable long mediumId,
                                  @RequestParam(required = false) boolean inactive,
                                  Pageable pageable, RedirectAttributes redirectAttributes) {
        if (!mediumConnectionService.existsById(mediumId)) {
            return redirectToMainPageWithErrorMessageCode(redirectAttributes,
                    "error.connectionNotExists");
        }
        model.addAttribute(Attributes.NAMES, mediumConnectionService.getMeterNumbers(mediumId, inactive, pageable));
        model.addAttribute(Attributes.REDIRECT_PAGE, Mappings.METER_PAGE);
        model.addAttribute(Attributes.MEDIUM_NAME, mediumConnectionService.getMediumName(mediumId));
        return Views.MEDIUM_METERS;
    }

    @GetMapping(Mappings.MEDIUM_PAGE + "/{mediumId}")
    public String showMedumConnection(Model model, @PathVariable long mediumId, RedirectAttributes redirectAttributes) {
        if (!mediumConnectionService.existsById(mediumId)) {
            return redirectToMainPageWithErrorMessageCode(redirectAttributes,
                    "error.connectionNotExists");
        }
        model.addAttribute(Attributes.MEDIUM_NAME, mediumConnectionService.getMediumName(mediumId));
        return Views.MEDIUM;
    }

    @GetMapping(Mappings.MEDIA_ADD)
    public String addNewMedium() {
        return Views.ADD_MEDIUM;
    }


    @PostMapping(Mappings.MEDIA_ADD)
    public String addNewMedium(@RequestParam(name = Attributes.MEDIUM_NAME) @NotBlank String mediumName) {
        mediumConnectionService.save(mediumName);
        return "redirect:" + Mappings.MEDIA_PAGE;
    }

    @GetMapping(Mappings.MEDIUM_PAGE + "/{mediumId}" + Mappings.METERS_ADD_SUBPAGE)
    public String addNewMediumMeter(@PathVariable Long mediumId, Model model) {
        model.addAttribute(Attributes.MEDIUM_METER_FORM, new MediumMeterForm());
        return Views.METER_ADD;
    }


    @PostMapping(Mappings.MEDIUM_PAGE + "/{mediumId}" + Mappings.METERS_ADD_SUBPAGE)
    public String addNewMediumMeter(@PathVariable Long mediumId,
                                    @ModelAttribute("mediumMeterForm") @Valid MediumMeterForm mediumMeterForm,
                                    BindingResult bindingResult,
                                    RedirectAttributes ra) {

        if (bindingResult.hasErrors()) {
            return Views.METER_ADD;
        }
        try {
            mediumConnectionService.addMediumMeter(mediumId, mediumMeterForm);
        } catch (Exception e) {
            return redirectToMainPageWithErrorMessageCode(ra, e.getMessage());
        }

        return "redirect:" + Mappings.MEDIUM_PAGE + "/" + mediumId + Mappings.METERS_SUBPAGE;
    }

    @GetMapping(Mappings.METER_PAGE + "/{meterId}")
    public String showMediumMeter(@PathVariable("meterId") long meterId, Model model,
                                  RedirectAttributes redirectAttributes) {
        return mediumMeterService.findById(meterId).map(meter -> {
            model.addAttribute("mediumMeter", meter);
            return Views.METER;
        }).orElseGet(() ->
            redirectToMainPageWithErrorMessageCode(redirectAttributes, "error.meterNotExists"));
    }

    private String redirectToMainPageWithErrorMessageCode(RedirectAttributes ra, String errorMessageCode) {
        ra.addAttribute("error", errorMessageCode);
        return "redirect:" + Mappings.MEDIA_PAGE;
    }


}
