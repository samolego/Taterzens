---
title: 代码文档
---

# ~~Javadoc~~ Dokadoc

---

# 由JetBrains设计的全新[文档风格](https://samolego.github.io/Taterzens/dokka/)

如果你想了解到快照版添加了什么，你可以在下方找到~~javadoc~~ dokadoc：

<ul>
    <li>
        <a id="release">
            <b id="releaseName">(最新版)</b>
        </a>
    </li>
    <li>
        <a href="https://samolego.github.io/Taterzens/dokka/latest-snapshot">
            最新快照
        </a>
    </li>
</ul>
	
<label for="versions">选择其他版本：</label>
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
