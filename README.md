# Java Pokedex

## Plan

1. Show spinner
2. [Load all the pokemon names, URLs + imgs](https://pokeapi.co/api/v2/pokemon?limit=100000&offset=0)
3. Sort alphabetically
4. Hide spinner + show search UI
5. On each keypress filter out out loaded names (plus sort alphabetically so they don't shift around too much)
<!-- -->
6. On pokemon click open details popup
7. Show spinner
8. [Load in data from special URL + download cry](https://pokeapi.co/api/v2/pokemon/1/)
9. Format + display data
10. Hide spinner + show detail UI (use prev downloaded pic)
11. On back btn pressed return to prev page

- OOP architecture (based on files from lectures and so on)
- resize hardcoded pokedex logo
- save pic and cry files in a `tmp` folder next to app (lite cache)
- better api thing for getting either latest or legacy cry if the former doesn't exist
- implement catching connection, status (`urlCon`), content and so on errors + showing them in popup dialog with short message 
- in cases where you're loading data (or doing other work) if it's not separated on to a different thread the UI will hang (read about how to separate them and send data over)
- [searchbar](https://stackoverflow.com/questions/19868287/how-can-i-make-a-search-box-in-java)
- better fonts and colors settings
	- <https://mycolor.space/?hex=%23DB2F42&sub=1>
	- <https://www.reddit.com/r/java/comments/122u4p/what_is_a_jlabels_default_font/>
	- <https://alvinalexander.com/blog/post/jfc-swing/swing-faq-list-fonts-current-platform/>