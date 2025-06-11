import json

# Load JSON from file
with open('api.json', 'r') as f:
    data = json.load(f)

# Begin Java array declaration
print('String[] imageEntries = {')

# Process each Pokémon entry
for result in data["results"]:
    url = result["url"]
    # Extract ID from URL (it's the last part before the final slash)
    poke_id = url.rstrip('/').split('/')[-1]
    image_url = f"https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/{poke_id}.png"
    filename = f"{poke_id}.png"
    print(f'    "{image_url}",')

# End array
print('};')

