model segregation

import "../include/schelling_common.gaml" 
global {
	list<space> free_places <- [] ;  
	list<space> all_places <- [] ;
	int neighbours_distance <- 50 min: 1 parameter: "Distance of perception:" category: "Population" max: 1000;
	file shape_file_name <- file("../gis/nha2.shp") parameter: "Shapefile to load:" category: "GIS specific";
	geometry shape <- envelope(shape_file_name);
	int square_meters_per_people <- 200 parameter: "Occupancy of people (in m2):" category: "GIS specific";
	int dimensions;
	action initialize_people { 
		create space from: shape_file_name with: [surface :: float(read("AREA"))];
		all_places  <- shuffle(space);
		number_of_people <- density_of_people * sum (all_places collect ((each as space).capacity)); 
		create people number: number_of_people;  
		ask people as list {  
			do move_to_new_place;      
		}   
		all_people <- people as list;  
	}      
	
	action initialize_places {}   
	
} 
entities {      
	species people parent: base {   
		
		
		const size type: float <- 2.0;  
		const color type: rgb <- colors at (rnd (number_of_groups - 1)); 
		const red type: int <- color as list at 0; 
		const green type: int <- color as list at 1;  
		const blue type: int <- color as list at 2; 
		space current_building <- nil;
		list<people> my_neighbours -> {people at_distance neighbours_distance}; 

		action move_to_new_place {  
			current_building <- (shuffle(all_places) first_with (((each).capacity) > 0));
			ask current_building {
				do accept one_people: myself;   
			}
		}
		reflex migrate when: !is_happy {
			if current_building != nil {
				ask current_building { 
					do remove_one one_people: myself;
				}
			} 
			do move_to_new_place;
		}
		aspect simple {
			draw circle(5);
		}
	}
	species space {	
		list<people> insiders <- [];
		rgb color <- rgb(255, 255, 255); 
		float surface;
		int capacity  <- 1 + int(surface / square_meters_per_people);    
		action accept {
			arg one_people type: people;
			add one_people to: insiders of self;
			location of (one_people as people) <- any_location_in(shape);
			capacity <- capacity - 1;
		}
		action remove_one (people one_people){
			remove one_people from: insiders of self;
			capacity <- capacity + 1;
		}
		aspect simple {
			rgb color <- empty(insiders) ? rgb("white") : rgb ([mean (insiders collect each.red), mean (insiders collect each.green), mean (insiders collect each.blue)]);
			draw  square(40) color: color;
		}
		aspect gis {
			rgb color <- empty(insiders) ? rgb("white") : rgb( [mean (insiders collect each.red), mean (insiders collect each.green), mean (insiders collect each.blue)]);
			draw shape color: color depth: length(insiders) * 10 border: rgb("black");
		} 
	}
}

experiment schelling type: gui {	
	output {
		display Town_display refresh_every: 1 {
			species space aspect: gis;
			species people  aspect: simple;
		}
		display Charts {
			chart name: "Proportion of happiness" type: pie background: rgb("lightGray") style: exploded position: {0,0} size: {1.0,0.5}{
				data "Unhappy" value: number_of_people - sum_happy_people color: rgb("green");
				data "Happy" value: sum_happy_people color: rgb("yellow") ;
			}
			chart name: "Global happiness and similarity" type: series background: rgb("lightGray") axes: rgb("white") position: {0,0.5} size: {1.0,0.5} {
				data "happy" color: rgb("blue") value:  (sum_happy_people / number_of_people) * 100 style: spline ;
				data "similarity" color: rgb("red") value: float (sum_similar_neighbours / sum_total_neighbours) * 100 style: step ;
			}
		}
	}
}
