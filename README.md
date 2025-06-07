# README

## Idea

**Main page with search bar showing names + thumbnails of critters
and then with each keypresses filtering them out based on present
characters alphabetically - by name (from the once loaded and
cached data), then after clicking on critter, opens popup window
with a few details on the picked critter + it's name and photo).**

Implement spinners and remember about error msgs (simple popup)
and object oriented programming architecture - based on files from
lectures.

## Pages

- [Search](https://pokeapi.co/api/v2/pokemon?limit=100000&offset=0)
- [Info](https://pokeapi.co/api/v2/pokemon/1/)
	- Name #ID
	- Species
	- Height / Weight (convert by dividing by `10` - `m / kg`)
	- Base stats
	- Abilities
	- [Picture (write algo to get best one 'official-artwork' > 'home' > 'sprite' and discriminate against GIFs + empty)](decidueye-hisui)
	- [Playable cry sound\*](https://raw.githubusercontent.com/PokeAPI/cries/main/cries/pokemon/latest/1.ogg)
