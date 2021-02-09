package com.aniamadej.sublokator.model;

import java.math.BigDecimal;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;

@Entity
@Table(name = "prices")
class Price {

  @EmbeddedId
  private PricePK pricePK;

  @MapsId("priceListId")
  @ManyToOne(optional = false)
  @JoinColumn(name = "priceListId", referencedColumnName = "id")
  private PriceList priceList;

  @MapsId("chargeId")
  @ManyToOne(optional = false)
  @JoinColumn(name = "chargeId", referencedColumnName = "id")
  private Charge charge;

  private BigDecimal amount;
  private BigDecimal taxRate;

}
