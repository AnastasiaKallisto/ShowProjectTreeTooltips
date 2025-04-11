# ShowProjectTreeTooltips for Rider

This plugin allows to see tooltips with short description of projects 
and C# files in .sln project tree in Rider.

- **.csproj files**: tag "Description" in .csproj
- **C# files**: first tag "summary" in .cs files which will be found before "class", "interface" and other c# types


It can be configured in settings (**File → Settings → Tools → Tooltips in project tree**)

## Example of work

### Tooltip for `.cs` file:

![CS Summary Tooltip](readmePictures/tooltip-cs-example.png)

### Tooltip for `.csproj`:

![CSPROJ Tooltip](readmePictures/tooltip-csproj-example.png)

### Settings
![Settings](readmePictures/settings.png)

## Installation

Install plugin + Java 17 (or later)

[![Rider](https://img.shields.io/jetbrains/plugin/v/RIDER_PLUGIN_ID.svg?label=Rider&colorB=0A7BBB&style=for-the-badge&logo=rider)](https://plugins.jetbrains.com/plugin/RIDER_PLUGIN_ID)


## Development

I used this template and followed the instructions: https://github.com/JetBrains/resharper-rider-plugin