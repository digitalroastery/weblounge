var PageMappings = {};

PageMappings.PageType = new Jsonix.Model.ClassInfo({
    name: "PageMappings.PageType"
});

PageMappings.Head = new Jsonix.Model.ClassInfo({
    name: "PageMappings.Head"
});

PageMappings.PageType.properties = [new Jsonix.Model.ElementPropertyInfo({
    name: "head",
    typeInfo: PageMappings.Head
})];

PageMappings.Head.properties = [new Jsonix.Model.ElementPropertyInfo({
    name: 'template',
    typeInfo: Jsonix.Schema.XSD.String.INSTANCE
}), new Jsonix.Model.ElementPropertyInfo({
    name: 'layout',
    typeInfo: Jsonix.Schema.XSD.String.INSTANCE
}), new Jsonix.Model.ElementPropertyInfo({
    name: 'promote',
    typeInfo: Jsonix.Schema.XSD.Boolean.INSTANCE
}), new Jsonix.Model.ElementPropertyInfo({
    name: 'index',
    typeInfo: Jsonix.Schema.XSD.Boolean.INSTANCE
})];

PageMappings.typeInfos = [PageMappings.Head];
PO.elementInfos = [{
    elementName: new Jsonix.XML.QName('page'),
    typeInfo: PageMappings.PageType
}];