package com.aniamadej.sublokator.model;

import java.io.Serializable;
import javax.persistence.Embeddable;

@Embeddable
class PricePK implements Serializable {

  private long priceListId;
  private long chargeId;
}
