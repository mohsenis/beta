/**
 * 
 * Copyright (C) 2012 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.gtfs.model;

import org.onebusaway.csv_entities.schema.annotations.CsvField;
import org.onebusaway.csv_entities.schema.annotations.CsvFields;
import org.onebusaway.gtfs.serialization.mappings.LatLonFieldMappingFactory;

@CsvFields(filename = "census.txt", required = false)
public final class CensusData extends IdentityBean<String> {

  private static final long serialVersionUID = 1L;
  
  private String id;

  private int population;

  @CsvField(mapping = LatLonFieldMappingFactory.class)
  private double lat;

  @CsvField(mapping = LatLonFieldMappingFactory.class)
  private double lon;
  
  public CensusData() {

  }

  public CensusData(CensusData obj) {
    this.id = obj.id;
    this.population = obj.population;
    this.lat = obj.lat;
    this.lon = obj.lon;
    
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public double getPopulation() {
    return population;
  }

  public void setPopulation(int population) {
    this.population = population;
  }

  public double getLat() {
    return lat;
  }

  public void setLat(double lat) {
    this.lat = lat;
  }

  public double getLon() {
    return lon;
  }

  public void setLon(double lon) {
    this.lon = lon;
  }

  @Override
  public String toString() {
    return "<Blockid " + this.id + ">";
  }
 
}
