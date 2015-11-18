#! /usr/bin/env bash

deepdive sql "update gdelt_raw_input set the_geom = ST_GeomFromText('POINT('|| actiongeo_long || ' ' || actiongeo_lat || ')',4326)";
deepdive sql "update acled_raw_input set the_geom = ST_GeomFromText('POINT('|| longitude || ' ' || latitude || ')',4326)";
