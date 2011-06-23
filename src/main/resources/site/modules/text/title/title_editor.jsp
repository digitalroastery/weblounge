Title: <input type="text" name="element:title" {if locale.current != null}value="\${ locale.current.text.title }"{/if} placeholder="\${ locale.original.text.title }" /><br />
Keyword: <input type="text" name="element:keyword" {if locale.current != null}value="\${ locale.current.text.keyword }"{/if} placeholder="\${ locale.original.text.keyword }" /><br />
Lead: <input type="text" name="element:lead" {if locale.current != null}value="\${ locale.current.text.lead }"{/if} placeholder="\${ locale.original.text.lead }" /><br />
Date: <input type="text" name="property:date" value="\${ properties.property.date }" /><br />
Author: <input type="text" name="property:author" value="\${ properties.property.author }" /><br />