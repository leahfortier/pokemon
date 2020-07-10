import re

from scripts.util import replace_special, namesies


# I don't know why this works for category as well as type but it does
def get_image_name(image_element):
    image_name = image_element.attrib["src"]
    return image_name[image_name.find("type") + 5: -4].capitalize()


# types should be an array that points to img elements
def get_types(type_images):
    assert len(type_images) == 1 or len(type_images) == 2

    types = ["NO_TYPE"] * 2
    for i, type_image in enumerate(type_images):
        # imageName is of the form "...type/<typeName>.gif"
        types[i] = namesies(get_image_name(type_image))

    return types


# Removes the form/forme suffix from the end
def normalize_form(form):
    return re.sub(" Forme?$", "", form).strip()


# Column indices should be specified as 1-indexed
def add_row_values(main_table, row_index, values, *column_indices):
    row = main_table[row_index]
    for column_index in column_indices:
        value = row.xpath('td')[column_index - 1].text.strip()
        add_value(values, value)


def add_value(values, value):
    value = value.strip()
    value = replace_special(value)
    values.append(value)
    print(value)
    return value


def get_element_text(element):
    text = element.text
    if text is not None:
        return text
    return element.text_content()


def get_schema_index(schema, column_name):
    for index, column in enumerate(schema.getchildren()):
        if column.text == column_name:
            return index


def get_query_text(query):
    for query_child in query:
        text = get_element_text(query_child)
        if text is not None:
            return text
        for child in query_child.getchildren():
            text = get_element_text(child)
            if text is not None:
                return text


def check_queries(table, *queries):
    for query_string in queries:
        query = table.xpath(query_string)
        text = get_query_text(query)
        if text is not None:
            return text


def check_header(table, header) -> bool:
    if table.tag == 'table':
        text = check_queries(table, 'tr[1]/td/b', 'tr[1]/td', 'thead/tr[1]/td')
        if text is not None and text == header:
            return True
    return False


def has_form(row, form_index, form_id):
    # No form index implies there is only the normal form or all forms are treated the same
    if form_index is None:
        return True

    for form in row[form_index][0][0].getchildren():
        if check_form(form[0], form_id):
            return True

    return False


def check_form(form, form_id):
    image_name = form.attrib["src"]
    if image_name.endswith('/' + form_id + '.png'):
        return True


def slash_form(cell_text: str, is_normal: bool) -> str:
    cell_text = cell_text.strip()
    split = cell_text.split('/')

    # No slash in text (only one form), just return the text
    if len(split) == 1:
        return cell_text
    # Multiple forms, but this is base form so use the first entry
    elif is_normal:
        return split[0].strip()
    # Multiple forms, but this is not base form so use the last entry
    else:
        return split[-1].strip()


# These have different names on serebii than in pokeapi so replacing here
def substitute_egg_group(egg_group: str) -> str:
    egg_group = namesies(egg_group.replace(' ', ''))
    if egg_group == "AMORPHOUS":
        return "INDETERMINATE"
    elif egg_group == "GRASS":
        return "PLANT"
    elif egg_group == "FIELD":
        return "GROUND"
    elif egg_group == "HUMAN_LIKE":
        return "HUMANSHAPE"
    else:
        return egg_group


def substitute_ability(ability_name: str) -> str:
    ability_name = namesies(ability_name)
    if ability_name == "COMPOUNDEYES":
        return "COMPOUND_EYES"
    else:
        return ability_name

