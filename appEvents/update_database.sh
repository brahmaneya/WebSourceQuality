#! /usr/bin/env bash

deepdive sql "update gdelt_input set the_geom = ST_GeomFromText('POINT('|| actiongeo_long || ' ' || actiongeo_lat || ')',4326)";
