# Film response references

Candid uses original film names and original resource art. The stock behavior is informed by published characteristics of real Kodak films, but Candid does **not** claim to be a colorimetrically exact emulation and does not redistribute Kodak packaging, logos, LUTs, or proprietary profiles.

## Candid Vivid 100
Reference character: fine-grain, high-sharpness, vivid 100-speed color negative film.

Design:
- very fine visible grain
- strong saturation
- crisp midtone contrast
- less forgiving in deep underexposure than the portrait stocks

## Candid Golden 200
Reference character: Kodak GOLD 200.

Kodak describes GOLD 200 as a low-speed color-negative film with saturated color, fine grain, high sharpness, and wide exposure latitude from about two stops under to three stops over.

Design:
- warm/yellow-red bias
- bright consumer-film saturation
- fine grain
- stronger highlight latitude than shadow latitude

Source:
https://kodakprofessional.com/sites/default/files/wysiwyg/pro/resources/E7022%20Gold%20tech%20sheet.pdf

## Candid Everyday 400
Reference character: Kodak ULTRA MAX 400.

Kodak describes ULTRA MAX 400 as a flexible high-speed consumer film with bright/vibrant color, fine grain, high sharpness, and improved underexposure protection.

Design:
- punchier color and contrast than Portrait 400
- moderate 400-speed grain
- useful shadow latitude
- general-purpose indoor/outdoor response

Source:
https://www.kodakprofessional.com/sites/default/files/wysiwyg/KodakUltraMax400TechSheet-1.pdf

## Candid Portrait 400
Reference character: Kodak Professional PORTRA 400.

Kodak describes PORTRA 400 as a true ISO 400 film with very fine grain, natural skin-tone reproduction, strong color, and excellent performance over a wide range of lighting conditions.

Design:
- restrained contrast
- natural, slightly warm color
- fine grain for ISO 400
- soft highlight shoulder and wide exposure latitude

Source:
https://www.kodakprofessional.com/photographers/film/color/kodak-professional-portra-400-film/516

## Candid Portrait 800
Reference character: Kodak Professional PORTRA 800.

Kodak describes PORTRA 800 as a high-speed color-negative film with natural color, fine grain for its speed, and especially strong underexposure latitude; Kodak also notes it can be pushed to 1600.

Design:
- more grain than Portrait 400
- low-light oriented response
- stronger shadow retention than the consumer stocks
- gentle highlight rolloff and natural saturation

Source:
https://www.kodakprofessional.com/photographers/film/color/kodak-professional-portra-800-film/528

## Candid Classic Mono 400
Reference character: Kodak Professional TRI-X 400.

Kodak describes TRI-X 400 as a classic-grain, sharp, wide-latitude panchromatic black-and-white film suitable for low light and action, with pushability to EI 1600.

Design:
- obvious traditional grain structure
- punchier midtone contrast
- broad B&W latitude
- strong documentary/street-photography character

Source:
https://www.kodakprofessional.com/photographers/film/black-white/kodak-professional-tri-x-films/515

## Candid Fine Mono 400
Reference character: Kodak Professional T-MAX 400.

Kodak describes T-MAX 400 as an exceptionally sharp, fine-grained ISO 400 black-and-white film with high resolving power and useful push latitude.

Design:
- finer grain than Classic Mono 400
- cleaner tonal transitions
- slightly gentler midtone bite
- high perceived sharpness at the Minecraft-map resolution

Source:
https://www.kodakprofessional.com/photographers/film/black-white/kodak-professional-t-max-400-film/526

## Simulation notes
Candid's photograph pipeline models film behavior before converting the image to Minecraft's limited 128x128 map palette. It includes:
- ISO-specific grain strength
- luminance-heavy grain plus smaller chroma-grain components on color stocks
- stronger apparent grain in shadows and with underexposure
- stock-specific saturation, contrast, and RGB response
- a compressed highlight shoulder instead of immediate digital clipping
- shadow toe behavior
- different overexposure and underexposure response by stock
- mild color contamination in underexposed color-negative film

The limited Minecraft map palette means these behaviors are intentionally tuned for a believable visual result in-game rather than laboratory sensitometric matching.
