from importlib.resources import Package
from typing_extensions import override

from hexdoc.core import Properties
from hexdoc.plugin import (
    HookReturn,
    LoadTaggedUnionsImpl,
    ModPlugin,
    ModPluginImplWithProps,
    ModPluginWithBook,
    hookimpl,
)

import hexdoc_slate_work

from .__gradle_version__ import FULL_VERSION, GRADLE_VERSION
from .__version__ import PY_VERSION
from ._export import generated
from .patchouli import pages


class Slate_workPlugin(ModPluginImplWithProps, LoadTaggedUnionsImpl):
    @staticmethod
    @hookimpl
    def hexdoc_mod_plugin(branch: str, props: Properties) -> ModPlugin:
        return Slate_workModPlugin(branch=branch, props=props)
    
    @staticmethod
    @hookimpl
    def hexdoc_load_tagged_unions() -> HookReturn[Package]:
        return [pages]


class Slate_workModPlugin(ModPluginWithBook):
    @property
    @override
    def modid(self) -> str:
        return "slate_work"

    @property
    @override
    def full_version(self) -> str:
        return FULL_VERSION

    @property
    @override
    def mod_version(self) -> str:
        return GRADLE_VERSION

    @property
    @override
    def plugin_version(self) -> str:
        return PY_VERSION

    @override
    def resource_dirs(self) -> HookReturn[Package]:
        return [generated]
    
    @override
    def jinja_template_root(self) -> tuple[Package, str]:
        return hexdoc_slate_work, "_templates"
