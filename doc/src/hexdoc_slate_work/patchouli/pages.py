from hexdoc.patchouli.page import PageWithTitle


class ExamplePage(PageWithTitle, type="slate_work:example"):
    """This is the Pydantic model for the `slate_work:example` page type."""

    example_value: str
