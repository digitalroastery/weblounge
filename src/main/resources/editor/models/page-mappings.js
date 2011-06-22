var PageMappings = {};

PageMappings.PagesCollection = new Jsonix.Model.ClassInfo({
	name: 'PageMappings.PagesCollection'
});

PageMappings.PageType = new Jsonix.Model.ClassInfo({
  name: 'PageMappings.PageType'
});

PageMappings.User = new Jsonix.Model.ClassInfo({
  name: 'PageMappings.User'
});

PageMappings.CreatedModified = new Jsonix.Model.ClassInfo({
  name: 'PageMappings.CreatedModified'
});

PageMappings.Published = new Jsonix.Model.ClassInfo({
  name: 'PageMappings.Published'
});

PageMappings.Security = new Jsonix.Model.ClassInfo({
  name: 'PageMappings.Security'
});

PageMappings.Security.Owner = new Jsonix.Model.ClassInfo({
  name: 'PageMappings.Security.Owner'
});

PageMappings.Locked = new Jsonix.Model.ClassInfo({
	name: 'PageMappings.Locked'
});

PageMappings.Head = new Jsonix.Model.ClassInfo({
  name: 'PageMappings.Head'
});

PageMappings.Head.Metadata = new Jsonix.Model.ClassInfo({
  name: 'PageMappings.Head.Metadata'
});

PageMappings.Body = new Jsonix.Model.ClassInfo({
  name: 'PageMappings.Body'
});

PageMappings.Body.Composer = new Jsonix.Model.ClassInfo({
  name: 'PageMappings.Body.Composer'
});

PageMappings.Body.Composer.Pagelet = new Jsonix.Model.ClassInfo({
  name: 'PageMappings.Body.Composer.Pagelet'
});

PageMappings.Body.Composer.Pagelet.Locale = new Jsonix.Model.ClassInfo({
  name: 'PageMappings.Body.Composer.Pagelet.Locale'
});

PageMappings.Body.Composer.Pagelet.Property = new Jsonix.Model.ClassInfo({
	name: 'PageMappings.Body.Composer.Pagelet.Property'
});

// <user id="my.id" realm="weblounge">My Name</user>
PageMappings.User.properties = [new Jsonix.Model.AttributePropertyInfo({
  name: 'id',
  typeInfo: Jsonix.Schema.XSD.String.INSTANCE
}), new Jsonix.Model.AttributePropertyInfo({
  name: 'realm',
  typeInfo: Jsonix.Schema.XSD.String.INSTANCE
}), new Jsonix.Model.ValuePropertyInfo({
  name: 'name',
  typeInfo: Jsonix.Schema.XSD.String.INSTANCE
})];

// <created|modified><user .../><date.../></created|modified>
PageMappings.CreatedModified.properties = [new Jsonix.Model.ElementPropertyInfo({
  name: 'user',
  typeInfo: PageMappings.User
}), new Jsonix.Model.ElementPropertyInfo({
  name: 'date',
  typeInfo: Jsonix.Schema.XSD.DateTime.INSTANCE
})];

PageMappings.Published.properties = [new Jsonix.Model.ElementPropertyInfo({
  name: 'user',
  typeInfo: PageMappings.User
}), new Jsonix.Model.ElementPropertyInfo({
  name: 'from',
  typeInfo: Jsonix.Schema.XSD.DateTime.INSTANCE
}), new Jsonix.Model.ElementPropertyInfo({
  name: 'to',
  typeInfo: Jsonix.Schema.XSD.DateTime.INSTANCE
})];

PageMappings.Security.properties = [new Jsonix.Model.ElementPropertyInfo({
	name: 'owner',
	typeInfo: PageMappings.Security.Owner
})];

PageMappings.Security.Owner.properties = [new Jsonix.Model.ElementPropertyInfo({
	name: 'user',
	typeInfo: PageMappings.User
})];

PageMappings.Locked.properties = [new Jsonix.Model.ElementPropertyInfo({
	name: 'user',
	typeInfo: PageMappings.User
})]

PageMappings.PagesCollection.properties = [new Jsonix.Model.ElementPropertyInfo({
	name: 'page',
	collection: true,
	typeInfo: PageMappings.PageType
})];

// <page id="15984df5-dfd5-68df-df5884" path="/foo/bar" version="live"><head ... /><body.../></page>
PageMappings.PageType.properties = [new Jsonix.Model.AttributePropertyInfo({
  name: 'id',
  typeInfo: Jsonix.Schema.XSD.String.INSTANCE
}), new Jsonix.Model.AttributePropertyInfo({
  name: 'path',
  typeInfo: Jsonix.Schema.XSD.String.INSTANCE
}), new Jsonix.Model.AttributePropertyInfo({
  name: 'version',
  typeInfo: Jsonix.Schema.XSD.String.INSTANCE
}), new Jsonix.Model.ElementPropertyInfo({
  name: 'head',
  typeInfo: PageMappings.Head
}), new Jsonix.Model.ElementPropertyInfo({
  name: 'body',
  typeInfo: PageMappings.Body
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
}), new Jsonix.Model.ElementPropertyInfo({
  name: 'metadata',
  typeInfo: PageMappings.Head.Metadata
}), new Jsonix.Model.ElementPropertyInfo({
  name: 'created',
  typeInfo: PageMappings.CreatedModified
}), new Jsonix.Model.ElementPropertyInfo({
  name: 'modified',
  typeInfo: PageMappings.CreatedModified
}), new Jsonix.Model.ElementPropertyInfo({
  name: 'published',
  typeInfo: PageMappings.Published
}), new Jsonix.Model.ElementPropertyInfo({
	name: 'security',
	typeInfo: PageMappings.Security
}), new Jsonix.Model.ElementPropertyInfo({
	name: 'locked',
	typeInfo: PageMappings.Locked
})];

PageMappings.Head.Metadata.properties = [new Jsonix.Model.ElementMapPropertyInfo({
	name: "title",
	key : new Jsonix.Model.AttributePropertyInfo({
		name : "language",
		typeInfo : Jsonix.Schema.XSD.String.INSTANCE
	}),
	value : new Jsonix.Model.ValuePropertyInfo({
		name : "value",
		typeInfo : Jsonix.Schema.XSD.String.INSTANCE
	})
}), new Jsonix.Model.ElementPropertyInfo({
  name: 'subject',
  collection: true,
  typeInfo: Jsonix.Schema.XSD.String.INSTANCE
}), new Jsonix.Model.ElementMapPropertyInfo({
	name: "description",
	key : new Jsonix.Model.AttributePropertyInfo({
		name : "language",
		typeInfo : Jsonix.Schema.XSD.String.INSTANCE
	}),
	value : new Jsonix.Model.ValuePropertyInfo({
		name : "value",
		typeInfo : Jsonix.Schema.XSD.String.INSTANCE
	})
}), new Jsonix.Model.ElementPropertyInfo({
	name: 'type',
	typeInfo: Jsonix.Schema.XSD.String.INSTANCE
}), new Jsonix.Model.ElementMapPropertyInfo({
	name: "coverage",
	key : new Jsonix.Model.AttributePropertyInfo({
		name : "language",
		typeInfo : Jsonix.Schema.XSD.String.INSTANCE
	}),
	value : new Jsonix.Model.ValuePropertyInfo({
		name : "value",
		typeInfo : Jsonix.Schema.XSD.String.INSTANCE
	})
}), new Jsonix.Model.ElementMapPropertyInfo({
	name: "rights",
	key : new Jsonix.Model.AttributePropertyInfo({
		name : "language",
		typeInfo : Jsonix.Schema.XSD.String.INSTANCE
	}),
	value : new Jsonix.Model.ValuePropertyInfo({
		name : "value",
		typeInfo : Jsonix.Schema.XSD.String.INSTANCE
	})
})];

// <body>...</body>
PageMappings.Body.properties = [new Jsonix.Model.ElementPropertyInfo({
	name: 'composers',
	collection: true,
	elementName: new Jsonix.XML.QName('composer'),
	typeInfo: PageMappings.Body.Composer
})];

// <composer id="composer-id">...</composer>
PageMappings.Body.Composer.properties = [new Jsonix.Model.AttributePropertyInfo({
  name: 'id',
  typeInfo: Jsonix.Schema.XSD.String.INSTANCE
}), new Jsonix.Model.ElementPropertyInfo({
  name: 'pagelets',
  collection: true,
  elementName: new Jsonix.XML.QName('pagelet'),
  typeInfo: PageMappings.Body.Composer.Pagelet
})
];

// <pagelet module="text" id="title">...</pagelet>
PageMappings.Body.Composer.Pagelet.properties = [new Jsonix.Model.AttributePropertyInfo({
  name: 'module',
  typeInfo: Jsonix.Schema.XSD.String.INSTANCE
}), new Jsonix.Model.AttributePropertyInfo({
  name: 'id',
  typeInfo: Jsonix.Schema.XSD.String.INSTANCE
}), new Jsonix.Model.ElementPropertyInfo({
	name: 'security',
	typeInfo: PageMappings.Security
}), new Jsonix.Model.ElementPropertyInfo({
  name: 'created',
  typeInfo: PageMappings.CreatedModified
}), new Jsonix.Model.ElementPropertyInfo({
  name: 'published',
  typeInfo: PageMappings.Published
}), new Jsonix.Model.ElementMapPropertyInfo({
	name: 'locale',
	key : new Jsonix.Model.AttributePropertyInfo({
		name : "key",
		typeInfo : Jsonix.Schema.XSD.String.INSTANCE
	}),
	value : PageMappings.Body.Composer.Pagelet.Locale
}), new Jsonix.Model.ElementPropertyInfo({
  name: 'properties',
  typeInfo: PageMappings.Body.Composer.Pagelet.Property
})];

PageMappings.Body.Composer.Pagelet.Locale.properties = [new Jsonix.Model.AttributePropertyInfo({
  name: 'language',
  typeInfo: Jsonix.Schema.XSD.String.INSTANCE
}), new Jsonix.Model.AttributePropertyInfo({
  name: 'original',
  typeInfo: Jsonix.Schema.XSD.Boolean.INSTANCE
}), new Jsonix.Model.ElementPropertyInfo({
  name: 'modified',
  typeInfo: PageMappings.CreatedModified
}), new Jsonix.Model.ElementMapPropertyInfo({
	name: 'text',
	key: new Jsonix.Model.AttributePropertyInfo({
		name: 'id',
		typeInfo: Jsonix.Schema.XSD.String.INSTANCE
	}),
	value: new Jsonix.Model.ValuePropertyInfo({
		name: 'value',
		typeInfo: Jsonix.Schema.XSD.String.INSTANCE
	})
})];

PageMappings.Body.Composer.Pagelet.Property.properties = [new Jsonix.Model.ElementMapPropertyInfo({
	name: 'property',
	key: new Jsonix.Model.AttributePropertyInfo({
		name: 'id',
		typeInfo: Jsonix.Schema.XSD.String.INSTANCE
	}),
	value: new Jsonix.Model.ValuePropertyInfo({
		name: 'value',
		typeInfo: Jsonix.Schema.XSD.String.INSTANCE
	})
})];

PageMappings.typeInfos = [PageMappings.Head];
PageMappings.elementInfos = [{
  elementName: new Jsonix.XML.QName('pages'),
  typeInfo: PageMappings.PagesCollection
},
{
  elementName: new Jsonix.XML.QName('page'),
  typeInfo: PageMappings.PageType
},
{
  elementName: new Jsonix.XML.QName('head'),
  typeInfo: PageMappings.Head
},
{
  elementName: new Jsonix.XML.QName('pagelet'),
  typeInfo: PageMappings.Body.Composer.Pagelet
}];