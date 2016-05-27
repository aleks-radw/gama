/**
* Name: ants_coupling
* Author: HUYNH Quang Nghi
* Description: This is the coupling of Ants model. It is supposed to use in the Comodeling Example as an interface. 
* Tags: comodel
*/
model ants_coupling

import "../../../Toy Models/Ants (Foraging and Sorting)/models/Ant Foraging (Complex).gaml"

//this is the experiment that supposed to uses
experiment Simple type: gui parent: Complete
{
	list<ant> get_ants
	{
		return list(ant);
	}

	list<ant_grid> get_ant_grid
	{
		return list(ant_grid);
	}
	//if we redefine the output, i.e, a blank output, the displays in parent experiment don't show.
	output
	{
	}

}