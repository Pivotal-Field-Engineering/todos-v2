# PACE Workshop Content

## Using Content

1. Determine what content would be of interest in your workshop by browsing this repo.

1. Use [PACE Builder](https://github.com/Pivotal-Field-Engineering/pace-builder) to build a workshop with this content.

1. *Please don't commit your customer workshop to this repo. This is a content repo not a place for my-awesome-customer-workshop*

## Adding Content

1. Determine if a folder exisits for the content you want to add. If not create one.

1. Add Markdown content to the folder. Ensure the markdown content ends with the correct language extension.
    - `*.en.md` - English
    - `*.es.md` - Spanish
    - `*.fr.md` - French
    - `*.pt.md` - Portuguese

**Note:** If only adding english content that will be the only language available in your workshop!

**Note: TLDR -- make your markdown filenames lowercase** There's a bit of a quirk with Markdown file names and Hugo, which renders the pages. Hugo creates its page routing in lowercase, while the `pace-builder` will generate one directory per Markdown page detected, and will use whatever case the filename had. The result is that uppercase letters in Markdown filenames can cause relative links to images/other files break, and routing in general to have some issues. As such please make your markdown filenames lowercase to avoid these issues.

***Give More Than You Take...***

<img src="https://amp.businessinsider.com/images/51c4bb22eab8ea4f5d000021-960-720.jpg" width="300">

