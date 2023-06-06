---
title: Code Documentation
---

# ~~Javadoc~~ Dokadoc

---

# [Fresh new docs](https://samolego.github.io/Taterzens/dokka/) styled by JetBrains

If you'd like to see what's happening behind the scenes,
you can find ~~javadoc~~ dokadoc on the following pages:

<ul>
    <li>
        <a id="release">
            <b id="releaseName">(latest release)</b>
        </a>
    </li>
    <li>
        <a href="https://samolego.github.io/Taterzens/dokka/latest-snapshot">
            latest snapshot
        </a>
    </li>
</ul>
	
<label for="versions">Choose other version:</label>
    <select name="versions" id="versions" onchange="gotoDocs(this)">
</select> 


<script>
    async function gotoDocs(select) {
        const url = select.value;
        if (url) {
            console.log("https://samolego.github.io/Taterzens/dokka/v" + url);
            window.location.href = "https://samolego.github.io/Taterzens/dokka/v" + url;
        }
    }

    const xhttp = new XMLHttpRequest();
    xhttp.onreadystatechange = function() {
        if (this.readyState == 4 && this.status == 200) {
            const data = JSON.parse(xhttp.responseText);
            const selector = document.getElementById("versions");

            const latest = data[0].tag_name;
            document.getElementById("release").href = "https://samolego.github.io/Taterzens/dokka/v" + latest;
            const current = document.getElementById("releaseName").innerHTML;
            document.getElementById("releaseName").innerHTML = "v" + latest + " "+ current;
            
            for (let i = 0; i < data.length; i++) {
                const release = data[i];
                const version = document.createElement("option");
                const tagName = release.tag_name;

                if (tagName === "1.7.0") {
                    // Docs only exist for 1.7.1 and up
                    return;
                }

                version.value = tagName;
                version.innerHTML = tagName;
                versions.append(version);
            }
        }
    };
    xhttp.open("GET", "https://api.github.com/repos/samolego/Taterzens/releases", true);
    xhttp.send();
</script>
