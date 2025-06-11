# Java Pokedex

A Java + swing Pokedex (a final project for the Programming languages and paradigms course at Uni).

## TODO

1. Flesh out endpoints
    1. Remove cry related stuff as that will not be implemented in the final version
    2. Include IDs for search data so it's easier to work with pics
    3. Implement as OOP API
2. Data irigation on pages via API + first joining of pages
3. [Implement searchbar](https://stackoverflow.com/questions/19868287/how-can-i-make-a-search-box-in-java)
4. [Figure out transitions between pages (modular) + in cases where you're doing other work, if not separated on different threads the UI will hang (read on how to separate them and send data between them)\*](https://www.google.com/search?sxsrf=AE3TifO4vN9F8fhqv4dKzkHq6lC2jZsf1A:1749624727079&q=what+is+swing+glass+pane)
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
5. Add connection, status (`urlCon`), content and so on err catches + showing them in popup dialog with short message 
<!-- -->
6. Finalize
    1. Add ver number in top bar\*
	2. Hard resize pokedex logo
	3. [Make sure it's OOP compliant (files from lectures and so on)](https://chatgpt.com/c/684809ed-71cc-8012-af80-2ef2483b0f6f)
	4. Flesh out fonts, colors etc.
		- <https://mycolor.space/?hex=%23DB2F42&sub=1>
		- <https://www.reddit.com/r/java/comments/122u4p/what_is_a_jlabels_default_font/>
		- <https://alvinalexander.com/blog/post/jfc-swing/swing-faq-list-fonts-current-platform/>
7. Send over
	1. README + Release tag
	2. Email
