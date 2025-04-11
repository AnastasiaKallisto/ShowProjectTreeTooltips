using System.Threading;
using JetBrains.Application.BuildScript.Application.Zones;
using JetBrains.ReSharper.Feature.Services;
using JetBrains.ReSharper.Psi.CSharp;
using JetBrains.ReSharper.TestFramework;
using JetBrains.TestFramework;
using JetBrains.TestFramework.Application.Zones;
using NUnit.Framework;

[assembly: Apartment(ApartmentState.STA)]

namespace ReSharperPlugin.ShowProjectTreeTooltips.Tests
{
    [ZoneDefinition]
    public class ShowProjectTreeTooltipsTestEnvironmentZone : ITestsEnvZone, IRequire<PsiFeatureTestZone>, IRequire<IShowProjectTreeTooltipsZone> { }

    [ZoneMarker]
    public class ZoneMarker : IRequire<ICodeEditingZone>, IRequire<ILanguageCSharpZone>, IRequire<ShowProjectTreeTooltipsTestEnvironmentZone> { }

    [SetUpFixture]
    public class ShowProjectTreeTooltipsTestsAssembly : ExtensionTestEnvironmentAssembly<ShowProjectTreeTooltipsTestEnvironmentZone> { }
}
